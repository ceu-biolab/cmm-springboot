package ceu.biolab.cmm.performance;

import ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest;
import ceu.biolab.cmm.browseSearch.service.BrowseSearchService;
import ceu.biolab.cmm.gcmsSearch.dto.GCMSSearchRequestDTO;
import ceu.biolab.cmm.gcmsSearch.service.GCMSSearchService;
import ceu.biolab.cmm.lcmsSearch.dto.BatchAdvancedSearchRequestDTO;
import ceu.biolab.cmm.lcmsSearch.service.BatchAdvancedSearchService;
import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMSSearch.service.MSMSSearchService;
import ceu.biolab.cmm.metadata.service.MetadataService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.test.annotation.DirtiesContext;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Opt-in local-DB measurements, separate from the regular correctness suite. */
@SpringBootTest(properties = {"logging.level.root=ERROR", "logging.level.ceu.biolab.cmm=ERROR", "logging.level.org.springframework=ERROR"})
@EnabledIfSystemProperty(named = "cmm.benchmark", matches = "true")
@DirtiesContext
class SearchPerformanceBenchmarkTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper mapper;
    @Autowired BatchAdvancedSearchService lcms;
    @Autowired GCMSSearchService gcms;
    @Autowired MSMSSearchService msms;
    @Autowired BrowseSearchService browse;
    @Autowired MetadataService metadata;

    @Test
    void measureRepresentativeSearches() throws Exception {
        DataSource original = jdbc.getDataSource();
        AtomicInteger queries = new AtomicInteger();
        jdbc.setDataSource(new CountingDataSource(original, queries));
        try {
            String label = System.getProperty("cmm.benchmark.label", "current");
            Path directory = Path.of("target/performance", label);
            Files.createDirectories(directory);
            Map<String, Object> measurements = new LinkedHashMap<>();
            measure(directory, measurements, queries, "lcms", () -> lcms.annotateAndScoreCmpoundsByMz(
                    read("lcmsSearch/requestLCMSSearch.json", BatchAdvancedSearchRequestDTO.class)));
            measure(directory, measurements, queries, "gcms", () -> gcms.search(
                    read("gcmsSearch/requestGCMSSearch.json", GCMSSearchRequestDTO.class)));
            measure(directory, measurements, queries, "msms", () -> msms.search(
                    read("msmsSearch/request_multiple_matches.json", MSMSSearchRequestDTO.class)));
            measure(directory, measurements, queries, "browse", () -> browse.search(
                    read("browseSearch/request1.json", BrowseSearchRequest.class)));
            measure(directory, measurements, queries, "stats", metadata::getDatabaseStats);
            mapper.writerWithDefaultPrettyPrinter().writeValue(directory.resolve("measurements.json").toFile(), measurements);
        } finally {
            jdbc.setDataSource(original);
        }
    }

    private <T> T read(String path, Class<T> type) throws Exception {
        try (var input = new ClassPathResource("json/" + path).getInputStream()) {
            return mapper.readValue(input, type);
        }
    }

    private void measure(Path directory, Map<String, Object> measurements, AtomicInteger queries,
                         String name, Callable<?> search) throws Exception {
        List<Double> times = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        JsonNode reference = null;
        for (int i = 0; i < 4; i++) {
            queries.set(0);
            long start = System.nanoTime();
            Object response = search.call();
            times.add((System.nanoTime() - start) / 1_000_000d);
            counts.add(queries.get());
            JsonNode actual = canonical(mapper.valueToTree(response));
            if (reference == null) reference = actual;
            else assertEquals(reference, actual, name + " must return the same content on repeated calls");
        }
        mapper.writerWithDefaultPrettyPrinter().writeValue(directory.resolve(name + "-response.json").toFile(), reference);
        List<Double> warm = times.subList(1, times.size()).stream().sorted().toList();
        measurements.put(name, Map.of("milliseconds", times, "queries", counts, "warmMedianMs", warm.get(1)));
        System.out.println("PERF " + name + " milliseconds=" + times + " queries=" + counts);
    }

    // Only array ordering is canonicalized. Scores, fields, duplicates and nulls
    // are retained so before/after snapshots can detect content changes.
    private JsonNode canonical(JsonNode node) {
        if (node.isObject()) {
            var result = mapper.createObjectNode();
            List<String> names = new ArrayList<>();
            node.fieldNames().forEachRemaining(names::add);
            names.stream().sorted().forEach(name -> result.set(name, canonical(node.get(name))));
            return result;
        }
        if (node.isArray()) {
            List<JsonNode> values = new ArrayList<>();
            node.forEach(value -> values.add(canonical(value)));
            values.sort(Comparator.comparing(JsonNode::toString));
            var result = mapper.createArrayNode();
            values.forEach(result::add);
            return result;
        }
        return node;
    }

    private static class CountingDataSource extends AbstractDataSource {
        private final DataSource delegate;
        private final AtomicInteger queries;

        CountingDataSource(DataSource delegate, AtomicInteger queries) {
            this.delegate = delegate;
            this.queries = queries;
        }

        @Override public Connection getConnection() throws java.sql.SQLException { return wrap(delegate.getConnection()); }
        @Override public Connection getConnection(String user, String password) throws java.sql.SQLException {
            return wrap(delegate.getConnection(user, password));
        }

        private Connection wrap(Connection connection) {
            return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[]{Connection.class}, (proxy, method, args) -> {
                try {
                    Object result = method.invoke(connection, args);
                    if (result instanceof Statement statement) {
                        Class<?> type = statement instanceof PreparedStatement ? PreparedStatement.class : Statement.class;
                        return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (p, m, a) -> {
                            if (m.getName().startsWith("execute")) queries.incrementAndGet();
                            try { return m.invoke(statement, a); }
                            catch (InvocationTargetException ex) { throw ex.getCause(); }
                        });
                    }
                    return result;
                } catch (InvocationTargetException ex) { throw ex.getCause(); }
            });
        }
    }
}
