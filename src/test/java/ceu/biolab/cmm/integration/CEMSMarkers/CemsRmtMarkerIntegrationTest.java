package ceu.biolab.cmm.integration.CEMSMarkers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class CemsRmtMarkerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void cemsRmt1MarkerEndpointMatchesStoredResponse() throws Exception {
        String requestJson = readResource("json/cemsMarkers/CEMSRMT1Marker_request1.json");
        String expectedJson = readResource("json/cemsMarkers/CEMSRMT1Marker_response1.json");

        MvcResult mvcResult = mockMvc.perform(post("/api/cems-rmt-1-marker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode actual = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        assertFixtureShape(actual, 5, 5);
        assertEquals(82106, firstHitCompoundId(actual));
        JSONAssert.assertEquals(expectedJson, actual.toString(), JSONCompareMode.STRICT);
    }

    @Test
    void cemsRmt2MarkerEndpointMatchesStoredResponse() throws Exception {
        String requestJson = readResource("json/cemsMarkers/CEMSRMT2Marker_request1.json");
        String expectedJson = readResource("json/cemsMarkers/CEMSRMT2Marker_response1.json");

        MvcResult mvcResult = mockMvc.perform(post("/api/cems-rmt-2-marker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode actual = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        assertFixtureShape(actual, 5, 4);
        assertEquals(82106, firstHitCompoundId(actual));
        JSONAssert.assertEquals(expectedJson, actual.toString(), JSONCompareMode.STRICT);
    }

    @Test
    void cemsRmt1MarkerEndpointRejectsMzToleranceAbove100() throws Exception {
        String requestJson = readResource("json/cemsMarkers/CEMSRMT1Marker_request1.json")
                .replace("\"tolerance\": 10", "\"tolerance\": 101");

        mockMvc.perform(post("/api/cems-rmt-1-marker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cemsRmt2MarkerEndpointRejectsMzToleranceAbove100() throws Exception {
        String requestJson = readResource("json/cemsMarkers/CEMSRMT2Marker_request1.json")
                .replace("\"tolerance\": 10", "\"tolerance\": 101");

        mockMvc.perform(post("/api/cems-rmt-2-marker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    private void assertFixtureShape(JsonNode response, int featureCount, int annotationCount) {
        JsonNode features = response.path("ceFeatures");
        assertEquals(featureCount, features.size());
        assertEquals(annotationCount, countAnnotations(features));
    }

    private int countAnnotations(JsonNode features) {
        int total = 0;
        for (JsonNode feature : features) {
            for (JsonNode group : feature.path("annotationsByAdducts")) {
                total += group.path("annotations").size();
            }
        }
        return total;
    }

    private long firstHitCompoundId(JsonNode response) {
        return response.path("ceFeatures").get(0)
                .path("annotationsByAdducts").get(0)
                .path("annotations").get(0)
                .path("compound")
                .path("compoundId")
                .asLong();
    }

    private String readResource(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
