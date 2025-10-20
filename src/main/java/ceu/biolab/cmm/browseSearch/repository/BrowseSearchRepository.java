package ceu.biolab.cmm.browseSearch.repository;


import ceu.biolab.cmm.browseSearch.dto.BrowseQueryResponse;
import ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest;
import ceu.biolab.cmm.msSearch.dto.CompoundDTO;
import ceu.biolab.cmm.msSearch.domain.compound.CompoundMapper;
import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.compound.CompoundType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class BrowseSearchRepository {
        private static final Logger LOGGER = LoggerFactory.getLogger(BrowseSearchRepository.class);

        private final NamedParameterJdbcTemplate jdbcTemplate;
        private final ResourceLoader resourceLoader;

        @Autowired
        public BrowseSearchRepository(NamedParameterJdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) {
            this.jdbcTemplate = jdbcTemplate;
            this.resourceLoader = resourceLoader;
        }

    public BrowseQueryResponse findMatchingCompounds(BrowseSearchRequest queryData) throws IOException {
        String sqlTemplate = loadSqlQuery("classpath:sql/browse_compound_search_query.sql");
        QueryParts queryParts = buildQuery(sqlTemplate, queryData);
        List<Compound> compounds = executeQuery(queryParts);
        return new BrowseQueryResponse(compounds);
        }

    private String loadSqlQuery(String resourcePath) throws IOException {
        Resource resource = resourceLoader.getResource(resourcePath);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private QueryParts buildQuery(String sqlTemplate, BrowseSearchRequest request) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = sqlTemplate;

        Set<Database> databases = request.getDatabases() == null ? Set.of() : request.getDatabases();
        List<String> databaseConditions = Database.databaseConditions(databases);
        StringBuilder filterCondition = new StringBuilder();

        if (!databaseConditions.isEmpty()) {
            filterCondition.append(" AND (").append(String.join(" OR ", databaseConditions)).append(")");
        }

        if (request.getMetaboliteType() == MetaboliteType.ONLYLIPIDS) {
            filterCondition.append(" AND c.compound_type = :compoundType");
            params.addValue("compoundType", CompoundType.LIPID.getDbValue());
        }
        sql = sql.replace("(:databaseFilterCondition)", filterCondition.toString());

        String compoundName = request.getCompoundName();
        if (compoundName != null && compoundName.trim().length() >= 3) {
            String operator = request.isExactName() ? "LIKE" : "ILIKE";
            String value = request.isExactName() ? compoundName : "%" + compoundName + "%";
            sql = sql.replace("(:compoundNameFilter)", "(c.compound_name " + operator + " :compoundName)");
            params.addValue("compoundName", value);
        } else {
            sql = sql.replace("AND (:compoundNameFilter)", "");
        }

        String formula = request.getFormula();
        params.addValue("formula", (formula == null || formula.isBlank()) ? "%" : formula);
        sql = sql.replace("'(:formula)'", ":formula");

        return new QueryParts(sql, params);
    }

    private List<Compound> executeQuery(QueryParts queryParts) {
        Set<Compound> compoundsSet = new HashSet<>();

        jdbcTemplate.query(queryParts.sql(), queryParts.params(), rs -> {
            while (rs.next()) {
                CompoundDTO dto = CompoundMapper.fromResultSet(rs);
                compoundsSet.add(CompoundMapper.toCompound(dto));
            }
            return compoundsSet;
        });

        LOGGER.info("browseSearch executed; matched {} compounds", compoundsSet.size());

        return new ArrayList<>(compoundsSet);
    }

    private record QueryParts(String sql, MapSqlParameterSource params) {}

}
