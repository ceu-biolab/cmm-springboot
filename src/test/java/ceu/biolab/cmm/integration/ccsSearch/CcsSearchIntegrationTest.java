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
        String requestJson = loadJson("json/ccsSearch/request_lcms_score.json");

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
    void testCcsSearchWithLcmsScoringAdductPenalty() throws Exception {
        String requestJson = loadJson("json/ccsSearch/request_lcms_score_penalty.json");

        MvcResult result = mockMvc.perform(post("/api/ccs/lcms-score")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode features = root.path("imFeatures");
        assertTrue(features.isArray());
        assertFalse(features.isEmpty());

        JsonNode firstAnnotationScores = features.get(0)
                .path("annotationsByAdducts")
                .get(0)
                .path("annotations")
                .get(0)
                .path("scores");

        assertTrue(firstAnnotationScores.isArray());
        assertFalse(firstAnnotationScores.isEmpty());
        JsonNode firstScore = firstAnnotationScores.get(0);
        assertTrue(firstScore.path("ionizationScore").asDouble() > 0.0,
                "Expected ionization score penalty when protonated adduct is missing");
    }
}
