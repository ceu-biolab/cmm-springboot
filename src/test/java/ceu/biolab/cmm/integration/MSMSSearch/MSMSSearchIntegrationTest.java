package ceu.biolab.cmm.integration.MSMSSearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        String expectedResponse = loadJson("json/msmsSearch/response_multiple_matches.json");

        MvcResult result = mockMvc.perform(post("/api/msms-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode expected = sortMsmsList(objectMapper.readTree(expectedResponse));
        JsonNode actual = sortMsmsList(objectMapper.readTree(result.getResponse().getContentAsString()));
        assertEquals(expected, actual);
    }

    @Test
    void testLcmsmsSearchAddsScoresToMsmsHits() throws Exception {
        String requestJson = loadJson("json/msmsSearch/request_lcmsms_score.json");
        String expectedResponse = loadJson("json/msmsSearch/response_lcmsms_score.json");

        MvcResult result = mockMvc.perform(post("/api/lcmsms-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode expected = sortLcmsmsResponse(objectMapper.readTree(expectedResponse));
        JsonNode actual = sortLcmsmsResponse(objectMapper.readTree(result.getResponse().getContentAsString()));
        assertEquals(expected, actual);
    }

    @Test
    void testLcmsmsSearchSupportsMultipleFeatures() throws Exception {
        String requestJson = loadJson("json/msmsSearch/request_lcmsms_score_multiple_features.json");
        String expectedResponse = loadJson("json/msmsSearch/response_lcmsms_score_multiple_features.json");

        MvcResult result = mockMvc.perform(post("/api/lcmsms-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode expected = sortLcmsmsResponse(objectMapper.readTree(expectedResponse));
        JsonNode actual = sortLcmsmsResponse(objectMapper.readTree(result.getResponse().getContentAsString()));
        assertEquals(expected, actual);
    }

    private JsonNode sortMsmsList(JsonNode response) {
        ObjectNode sortedResponse = response.deepCopy();
        List<JsonNode> hits = new ArrayList<>();
        response.path("msmsList").forEach(hits::add);
        hits.sort(Comparator.comparingInt(hit -> hit.path("msmsId").asInt()));

        ArrayNode sortedHits = objectMapper.createArrayNode();
        hits.forEach(sortedHits::add);
        sortedResponse.set("msmsList", sortedHits);
        return sortedResponse;
    }

    private JsonNode sortLcmsmsResponse(JsonNode response) {
        ObjectNode sortedResponse = response.deepCopy();
        JsonNode features = sortedResponse.path("msmsFeatures");
        if (features.isArray()) {
            ArrayNode sortedFeatures = objectMapper.createArrayNode();
            features.forEach(feature -> sortedFeatures.add(sortMsmsList(feature)));
            sortedResponse.set("msmsFeatures", sortedFeatures);
        }
        if (sortedResponse.has("msmsList")) {
            sortedResponse = (ObjectNode) sortMsmsList(sortedResponse);
        }
        return sortedResponse;
    }
}
