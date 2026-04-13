package ceu.biolab.cmm.integration.MSMSSearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class MSMSSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String loadJson(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    @Test
    void testMSMSSearchWithCompleteExample()  throws Exception {
        String requestJson = loadJson("json/msmsSearch/request1.json");
        String expectedResponse = loadJson("json/msmsSearch/response1.json");
        mockMvc.perform(post("/api/msms-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                    .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));
    }

    @Test
    void testMSMSSearchRejectsToleranceAbove100() throws Exception {
        String requestJson = loadJson("json/msmsSearch/request1.json")
                .replace("\"tolerancePrecursorIon\": 10.0", "\"tolerancePrecursorIon\": 101.0");

        mockMvc.perform(post("/api/msms-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMSMSSearchReturnsMultipleExperimentalIsobarMatches() throws Exception {
        String requestJson = loadJson("json/msmsSearch/request_multiple_matches.json");

        MvcResult result = mockMvc.perform(post("/api/msms-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode msmsList = response.path("msmsList");
        assertTrue(msmsList.isArray());
        assertTrue(msmsList.size() > 1, "Expected the isobaric amino-acid request to return multiple matches");
        assertEquals(9, response.path("experimentalSpectrum").path("peaks").size());

        Set<String> compoundNames = new HashSet<>();
        for (JsonNode hit : msmsList) {
            assertEquals("experimental", hit.path("spectrumSource").asText());
            assertTrue(hit.path("msmsCosineScore").asDouble() >= 0.5);
            compoundNames.add(hit.path("compound").path("compoundName").asText());
        }

        assertTrue(compoundNames.contains("L-Isoleucine"));
        assertTrue(compoundNames.contains("L-Alloisoleucine") || compoundNames.contains("L-Norleucine"));
    }
}
