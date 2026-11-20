package ceu.biolab.cmm.legacyApi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/** Stable legacy envelopes; response rows are assembled explicitly by the response mapper. */
public record LegacyResults(List<?> results) {
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record CeResults(List<?> compounds_found, List<?> fragments) {}
    public record Pathway(String name, String uri) {}
}
