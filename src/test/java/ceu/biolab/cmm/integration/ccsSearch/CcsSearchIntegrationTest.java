package ceu.biolab.cmm.integration.ccsSearch;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
public class CcsSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String loadJson(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    @Test
    void testCcsSearchEndpoint1() throws Exception {
        String requestJson = loadJson("json/ccsSearch/request1.json");
        String expectedResponse = loadJson("json/ccsSearch/response1.json");

        mockMvc.perform(post("/api/ccs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));
    }

    @Test
    void testCcsSearchEndpointWithMultipleAdducts() throws Exception {
        String requestJson = loadJson("json/ccsSearch/request2.json");
        String expectedResponse = loadJson("json/ccsSearch/response2.json");

        mockMvc.perform(post("/api/ccs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse, JsonCompareMode.LENIENT));
    }

    @Test
    void testCcsSearchEndpointNegativeMode() throws Exception {
        String requestJson = loadJson("json/ccsSearch/request3.json");
        String expectedResponse = loadJson("json/ccsSearch/response3.json");

        mockMvc.perform(post("/api/ccs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse, JsonCompareMode.LENIENT));
    }

    @Test
    void testCcsSearchWithLcmsScoring() throws Exception {
        String requestJson = loadJson("json/ccsSearch/request_lcimms_score.json");

        MvcResult result = mockMvc.perform(post("/api/ccs/lcms-score")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode features = root.path("imFeatures");
        assertTrue(features.isArray());
        assertFalse(features.isEmpty());

        JsonNode firstFeature = features.get(0);
        JsonNode annotationsByAdduct = firstFeature.path("annotationsByAdducts");
        assertTrue(annotationsByAdduct.isArray());
        JsonNode firstAnnotation = annotationsByAdduct.get(0).path("annotations");
        assertTrue(firstAnnotation.isArray());
        JsonNode scores = firstAnnotation.get(0).path("scores");
        assertTrue(scores.isArray());
        assertFalse(scores.isEmpty());
        JsonNode firstScore = scores.get(0);
        assertTrue(firstScore.isObject());
    }

    @Test
    void testCcsSearchWithLcmsScoringProducesRetentionScores() throws Exception {
        String requestJson = loadJson("json/ccsSearch/request_lcimms_score_with_scores.json");

        MvcResult result = mockMvc.perform(post("/api/ccs/lcms-score")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode features = root.path("imFeatures");
        assertTrue(features.isArray());
        assertFalse(features.isEmpty());

        boolean foundRetentionScore = false;

        for (JsonNode feature : features) {
            JsonNode annotationsByAdduct = feature.path("annotationsByAdducts");
            if (!annotationsByAdduct.isArray()) {
                continue;
            }
            for (JsonNode annotationsNode : annotationsByAdduct) {
                JsonNode annotations = annotationsNode.path("annotations");
                if (!annotations.isArray()) {
                    continue;
                }
                for (JsonNode annotation : annotations) {
                    JsonNode scores = annotation.path("scores");
                    if (!scores.isArray() || scores.isEmpty()) {
                        continue;
                    }
                    JsonNode score = scores.get(0);
                    JsonNode rtScoreMap = score.path("rtScoreMap");
                    if (rtScoreMap.isObject() && rtScoreMap.size() > 0) {
                        foundRetentionScore = true;
                        break;
                    }
                }
                if (foundRetentionScore) {
                    break;
                }
            }
            if (foundRetentionScore) {
                break;
            }
        }

        assertTrue(foundRetentionScore, "Expected at least one annotation to carry retention-time scores");
    }
}
