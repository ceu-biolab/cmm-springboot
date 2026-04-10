package ceu.biolab.cmm.integration.msSearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MSSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String loadJson(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    @Test
    void testMSSearchSimpleSearchEndpoint1() throws Exception {
        String requestJson = loadJson("json/msSearch/requestMSSearchSimple1.json");

        MvcResult result = mockMvc.perform(post("/api/compounds/simple-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode features = response.path("msfeatures");
        org.junit.jupiter.api.Assertions.assertEquals(1, features.size());

        JsonNode firstFeature = features.get(0);
        org.junit.jupiter.api.Assertions.assertEquals(
                List.of("[M+H]+", "[M+Na]+", "[M+NH4]+", "[M+K]+", "[M+H-H2O]+"),
                adductOrder(firstFeature)
        );
        org.junit.jupiter.api.Assertions.assertEquals(15, firstFeature.path("annotationsByAdducts").get(0).path("annotations").size());
        org.junit.jupiter.api.Assertions.assertEquals(0, firstFeature.path("annotationsByAdducts").get(1).path("annotations").size());
        org.junit.jupiter.api.Assertions.assertEquals("C43H81O8P",
                firstFeature.path("annotationsByAdducts").get(0).path("annotations").get(0).path("compound").path("formula").asText());
    }

    @Test
    void testMSSearchBatchSearchEndpoint1() throws Exception {
        String requestJson = loadJson("json/msSearch/requestMSSearchBatch1.json");

        MvcResult result = mockMvc.perform(post("/api/compounds/batch-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode features = response.path("msfeatures");
        org.junit.jupiter.api.Assertions.assertEquals(2, features.size());

        List<String> expectedAdductOrder = List.of("[M+H]+", "[M+Na]+", "[M+NH4]+", "[M+K]+", "[M+H-H2O]+", "[M+2H]2+");
        org.junit.jupiter.api.Assertions.assertEquals(expectedAdductOrder, adductOrder(features.get(0)));
        org.junit.jupiter.api.Assertions.assertEquals(expectedAdductOrder, adductOrder(features.get(1)));
        org.junit.jupiter.api.Assertions.assertEquals(15, features.get(0).path("annotationsByAdducts").get(0).path("annotations").size());
        org.junit.jupiter.api.Assertions.assertEquals(9, features.get(1).path("annotationsByAdducts").get(1).path("annotations").size());
        org.junit.jupiter.api.Assertions.assertEquals(66, features.get(1).path("annotationsByAdducts").get(4).path("annotations").size());
    }

    @Test
    void testMSSearchSimpleRejectsToleranceAbove100() throws Exception {
        String requestJson = """
                {
                  "mz": 757.5667,
                  "mzToleranceMode": "PPM",
                  "tolerance": 101,
                  "ionizationMode": "POSITIVE",
                  "adductsString": ["[M+H]+"],
                  "databases": ["ALL"],
                  "metaboliteType": "ALL"
                }
                """;

        mockMvc.perform(post("/api/compounds/simple-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetCompoundByIdEndpoint() throws Exception {
        mockMvc.perform(get("/api/compounds/33"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compoundId").value(33))
                .andExpect(jsonPath("$.compoundName").value("Ochtodane skeleton"));
    }

    @Test
    void testGetCompoundByIdEndpointNotFound() throws Exception {
        mockMvc.perform(get("/api/compounds/999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCompoundByIdEndpointInvalidId() throws Exception {
        mockMvc.perform(get("/api/compounds/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMSSearchSimpleAcceptsFormulaTypeFilter() throws Exception {
        String requestJson = loadJson("json/msSearch/requestMSSearchSimple1.json")
                .replace("\"databases\":", "\"formulaType\": \"CHNOPS\",\n  \"databases\":");

        MvcResult result = mockMvc.perform(post("/api/compounds/simple-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        assertAllReturnedCompoundsUseAlphabet(result.getResponse().getContentAsString(), Set.of("C", "H", "N", "O", "P", "S"));
    }

    @Test
    void testMSSearchBatchAcceptsFormulaTypeFilter() throws Exception {
        String requestJson = loadJson("json/msSearch/requestMSSearchBatch1.json")
                .replace("\"databases\":", "\"formulaType\": \"CHNOPS\",\n  \"databases\":");

        MvcResult result = mockMvc.perform(post("/api/compounds/batch-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        assertAllReturnedCompoundsUseAlphabet(result.getResponse().getContentAsString(), Set.of("C", "H", "N", "O", "P", "S"));
    }

    private void assertAllReturnedCompoundsUseAlphabet(String responseJson, Set<String> allowedElements) throws Exception {
        JsonNode features = objectMapper.readTree(responseJson).path("msfeatures");
        boolean sawFormula = false;

        for (JsonNode feature : features) {
            JsonNode annotationsByAdduct = feature.path("annotationsByAdducts");
            for (JsonNode byAdduct : annotationsByAdduct) {
                for (JsonNode annotation : byAdduct.path("annotations")) {
                    String formula = annotation.path("compound").path("formula").asText("");
                    if (formula.isBlank()) {
                        continue;
                    }
                    sawFormula = true;
                    Matcher matcher = FORMULA_ELEMENT_PATTERN.matcher(formula);
                    while (matcher.find()) {
                        org.junit.jupiter.api.Assertions.assertTrue(
                                allowedElements.contains(matcher.group(1)),
                                () -> "Unexpected element " + matcher.group(1) + " in formula " + formula
                        );
                    }
                }
            }
        }

        org.junit.jupiter.api.Assertions.assertTrue(sawFormula);
    }

    private List<String> adductOrder(JsonNode feature) {
        List<String> adducts = new ArrayList<>();
        for (JsonNode byAdduct : feature.path("annotationsByAdducts")) {
            adducts.add(byAdduct.path("adduct").asText());
        }
        return adducts;
    }

    private static final Pattern FORMULA_ELEMENT_PATTERN = Pattern.compile("([A-Z][a-z]?)");
}
