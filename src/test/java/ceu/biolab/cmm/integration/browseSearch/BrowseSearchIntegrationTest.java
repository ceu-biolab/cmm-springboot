package ceu.biolab.cmm.integration.browseSearch;

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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class BrowseSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String loadJson(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    // this is an example that should work
    @Test
    void testBrowseSearchWithCompleteExample() throws Exception {
        String requestJson = loadJson("json/browseSearch/request1.json");

        mockMvc.perform(post("/api/browse-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                        .andExpect(status().isOk());
    }
    @Test
    void testBrowseSearchWithNullName() throws Exception {
        String requestJson = loadJson("json/browseSearch/request2.json");
        mockMvc.perform(post("/api/browse-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compoundlist").isArray())
                .andExpect(jsonPath("$.compoundlist[?(@.compoundId == 95450)].pathways[0].pathwayId").exists())
                .andExpect(jsonPath("$.compoundlist[?(@.compoundId == 147897)].pathways[0].pathwayId").exists())
                .andExpect(jsonPath("$.compoundlist[?(@.compoundId == 82830)].pathways[0].pathwayId").exists());
    }

    @Test
    void testBrowseSearchWithNullFromula() throws Exception {
        String requestJson = loadJson("json/browseSearch/request3.json");
        mockMvc.perform(post("/api/browse-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compoundlist").isArray())
                .andExpect(jsonPath("$.compoundlist").isNotEmpty());
    }

    @Test
    void testBrowseSearchWithNameOnlyAndNoFormulaField() throws Exception {
        String requestJson = loadJson("json/browseSearch/request6_name_only.json");

        mockMvc.perform(post("/api/browse-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compoundlist[?(@.compoundId == 33)]").isNotEmpty());
    }

    @Test
    void testBrowseSearchWithNullDatabase() throws Exception {
        String requestJson = loadJson("json/browseSearch/request4.json");
        mockMvc.perform(post("/api/browse-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testBrowseSearchWithNullMetabolite() throws Exception {
        String requestJson = loadJson("json/browseSearch/request5.json");
        mockMvc.perform(post("/api/browse-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testBrowseSearchFiltersByFormulaType() throws Exception {
        String requestJson = """
                {
                  "compoundName": "chlor",
                  "databases": ["ALL"],
                  "metaboliteType": "ALL",
                  "exactName": false,
                  "formulaType": "CHNOPS"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/browse-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode compounds = objectMapper.readTree(result.getResponse().getContentAsString()).path("compoundlist");
        org.junit.jupiter.api.Assertions.assertTrue(compounds.isArray());
        org.junit.jupiter.api.Assertions.assertTrue(compounds.size() > 0);

        boolean sawFormula = false;
        for (JsonNode compound : compounds) {
            String formula = compound.path("formula").asText("");
            if (formula.isBlank()) {
                continue;
            }
            sawFormula = true;
            assertFormulaWithinAlphabet(formula, Set.of("C", "H", "N", "O", "P", "S"));
        }

        org.junit.jupiter.api.Assertions.assertTrue(sawFormula);
    }

    private void assertFormulaWithinAlphabet(String formula, Set<String> allowedElements) {
        Matcher matcher = FORMULA_ELEMENT_PATTERN.matcher(formula);
        while (matcher.find()) {
            org.junit.jupiter.api.Assertions.assertTrue(
                    allowedElements.contains(matcher.group(1)),
                    () -> "Unexpected element " + matcher.group(1) + " in formula " + formula
            );
        }
    }

    private static final Pattern FORMULA_ELEMENT_PATTERN = Pattern.compile("([A-Z][a-z]?)");
}
