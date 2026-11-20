package ceu.biolab.cmm.shared.repository;

import org.springframework.core.io.ResourceLoader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Reads packaged SQL through streams, once per repository and resource path. */
public final class SqlQueryLoader {
    private final ResourceLoader resources;
    private final ConcurrentMap<String, String> queries = new ConcurrentHashMap<>();

    public SqlQueryLoader(ResourceLoader resources) { this.resources = resources; }

    public String load(String path) throws IOException {
        try {
            return queries.computeIfAbsent(path, key -> {
                try (var stream = resources.getResource(key).getInputStream()) {
                    return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                } catch (IOException exception) {
                    throw new UncheckedIOException(exception);
                }
            });
        } catch (UncheckedIOException exception) {
            throw exception.getCause();
        }
    }
}
