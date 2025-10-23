package ceu.biolab.cmm.integration.scoreAnnotations;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ScoreAnnotationsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String loadJson(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    @Test
    void scoreLipidsRetainsAndScoresLipidAnnotations() throws Exception {
        String requestJson = loadJson("json/scoreAnnotations/request_simple.json");
        String expectedResponse = loadJson("json/scoreAnnotations/response_simple.json");

        mockMvc.perform(post("/api/score-annotations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedResponse, JsonCompareMode.LENIENT));
    }

    @Test
    void scoreLipidsWithoutSupportingAdductAppliesPenalty() throws Exception {
        String requestJson = loadJson("json/scoreAnnotations/request_lack_adduct.json");

        MvcResult result = mockMvc.perform(post("/api/score-annotations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(root.isArray());
        assertEquals(1, root.size());
        JsonNode adducts = root.get(0).path("annotationsByAdducts");
        assertTrue(adducts.isArray());
        JsonNode firstScore = adducts.get(0).path("annotations").get(0).path("scores").get(0);
        assertNotNull(firstScore);
        JsonNode adductScore = firstScore.path("adductRelationScore");
        assertTrue(adductScore.isNull(), "No supporting protonated adduct should leave the adduct relation score unset");

        JsonNode ionizationScore = firstScore.path("ionizationScore");
        assertTrue(ionizationScore.isNumber());
        assertEquals(0.5, ionizationScore.asDouble(), 1e-9);
    }

    @Test
    void scoreLipidsCapturesRetentionTimeViolations() throws Exception {
        String requestJson = loadJson("json/scoreAnnotations/request_rt_inconsistent.json");

        MvcResult result = mockMvc.perform(post("/api/score-annotations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        assertTrue(root.isArray());
        assertEquals(2, root.size());

        JsonNode fasterFeatureScores = root.get(0).path("annotationsByAdducts").get(0).path("annotations").get(0).path("scores").get(0);
        assertTrue(fasterFeatureScores.path("rtScoreMap").isObject());
        JsonNode rtScoresAgainstSlower = fasterFeatureScores.path("rtScoreMap").path("820.17.0");
        assertNotNull(rtScoresAgainstSlower, "Expected retention-time comparison recorded against the slower feature");
        assertTrue(rtScoresAgainstSlower.isArray());
        assertEquals(1, rtScoresAgainstSlower.size());
        assertFalse(rtScoresAgainstSlower.get(0).asBoolean(), "Higher double bonds should not have a higher retention time");

        JsonNode slowerFeatureScores = root.get(1).path("annotationsByAdducts").get(0).path("annotations").get(0).path("scores").get(0);
        JsonNode rtScoresAgainstFaster = slowerFeatureScores.path("rtScoreMap").path("820.15.0");
        assertNotNull(rtScoresAgainstFaster, "Expected retention-time comparison recorded against the faster feature");
        assertTrue(rtScoresAgainstFaster.isArray());
        assertEquals(1, rtScoresAgainstFaster.size());
        assertFalse(rtScoresAgainstFaster.get(0).asBoolean(), "Fewer double bonds should not have a lower retention time than their unsaturated pair");
    }
}
