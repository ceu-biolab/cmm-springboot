package ceu.biolab.cmm.integration.metadata;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;

@SpringBootTest
@AutoConfigureMockMvc
class MetadataIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getCcsAdductsReturnsCatalog() throws Exception {
        String expectedResponse = loadJson("json/metadata/ccs_adducts_response.json");

        mockMvc.perform(get("/api/metadata/ccs-adducts"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));
    }

    @Test
    void getCeMsBuffersReturnsList() throws Exception {
        String expectedResponse = loadJson("json/metadata/ce_ms_buffers_response.json");

        mockMvc.perform(get("/api/metadata/ce-ms-buffers"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));
    }

    @Test
    void getCeMsOptionsReturnsFullCatalog() throws Exception {
        String expectedResponse = loadJson("json/metadata/ce_ms_options_response.json");

        mockMvc.perform(get("/api/metadata/ce-ms-options"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));
    }

    @Test
    void getCeMsOptionsReturnsDbBackedRmtReferenceCompoundsForSelectedCondition() throws Exception {
        String expectedResponse = loadJson(
                "json/metadata/ce_ms_options_formic_acid_1m_20_direct_positive_response.json");

        mockMvc.perform(get("/api/metadata/ce-ms-options")
                        .param("buffer", "FORMIC_ACID_1M")
                        .param("temperature", "20")
                        .param("polarity", "Direct")
                        .param("ionization_mode", "Positive"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));
    }

    @Test
    void getCeMsOptionsRejectsInvalidPolarity() throws Exception {
        mockMvc.perform(get("/api/metadata/ce-ms-options")
                        .param("polarity", "Sideways"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDatabaseStatsReturnsCounts() throws Exception {
        String expectedResponse = loadJson("json/metadata/stats_response.json");

        mockMvc.perform(get("/api/metadata/stats"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));
    }

    private String loadJson(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }
}
