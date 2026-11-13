package ceu.biolab.cmm.integration.compoundDetails;

import ceu.biolab.cmm.msSearch.service.CompoundService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Read-only checks against the local PostgreSQL catalog, using the same profile as existing database tests. */
@SpringBootTest(properties = "spring.datasource.hikari.read-only=true")
@AutoConfigureMockMvc
@ActiveProfiles("local")
class CompoundDetailsIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired CompoundService compounds;
    @Autowired ObjectMapper json;

    @ParameterizedTest @CsvSource({"/classyfire/0000152,nodeId,CHEMONTID:0000152",
            "/ontology/7659,ontologyTermId,7659", "/references/1,referenceId,1", "/organisms/3,organismId,3"})
    void catalogRecordsAreReadThroughTheNewApi(String path,String property,String expected) throws Exception {
        var result = mvc.perform(get("/api"+path)).andExpect(status().isOk()).andReturn();
        assertEquals(expected,json.readTree(result.getResponse().getContentAsString()).path(property).asText());
    }
    @Test void compoundEnrichmentPreservesEveryExistingProperty() throws Exception {
        var baseline = json.valueToTree(compounds.getCompoundById(257));
        var result = mvc.perform(get("/api/compounds/257")).andExpect(status().isOk())
                .andExpect(jsonPath("$.references").isNotEmpty()).andExpect(jsonPath("$.reactions").isArray())
                .andExpect(jsonPath("$.ontologyTerms").isArray()).andExpect(jsonPath("$.classyfireClassification").isArray())
                .andExpect(jsonPath("$.organisms").isArray()).andReturn();
        var expanded = json.readTree(result.getResponse().getContentAsString());
        baseline.fields().forEachRemaining(field -> assertEquals(field.getValue(),expanded.get(field.getKey()),field.getKey()));
    }
    @Test void organismHierarchyStartsAtTheRequestedOrganismAndEndsAtRoot() throws Exception {
        var result = mvc.perform(get("/api/organisms/3")).andExpect(status().isOk()).andReturn();
        var hierarchy = json.readTree(result.getResponse().getContentAsString()).path("organismHierarchy");
        assertEquals("3",hierarchy.get(0).path("id").asText());
        assertEquals(1,hierarchy.get(hierarchy.size()-1).path("level").asInt());
    }
}
