package ceu.biolab.cmm.integration.metadata;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MetadataIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getCcsAdductsReturnsCatalog() throws Exception {
        mockMvc.perform(get("/api/metadata/ccs-adducts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.positive").isArray())
                .andExpect(jsonPath("$.negative").isArray())
                .andExpect(jsonPath("$.positive[?(@ == '[M+H]+')]").isNotEmpty())
                .andExpect(jsonPath("$.negative[?(@ == '[M-H]-')]").isNotEmpty());
    }

    @Test
    void getCeMsBuffersReturnsList() throws Exception {
        mockMvc.perform(get("/api/metadata/ce-ms-buffers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$[?(@.code == 'FORMIC_ACID_1M')]").isNotEmpty());
    }

    @Test
    void getDatabaseStatsReturnsCounts() throws Exception {
        mockMvc.perform(get("/api/metadata/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compounds").isNumber())
                .andExpect(jsonPath("$.compounds").value(org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.gcmsSpectra").isNumber())
                .andExpect(jsonPath("$.msmsSpectra").isNumber())
                .andExpect(jsonPath("$.ccsRecords").isNumber())
                .andExpect(jsonPath("$.cemsRecords").isNumber());
    }
}
