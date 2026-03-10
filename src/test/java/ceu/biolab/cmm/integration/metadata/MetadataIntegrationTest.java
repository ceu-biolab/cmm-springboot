package ceu.biolab.cmm.integration.metadata;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class MetadataIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        MvcResult result = mockMvc.perform(get("/api/metadata/ce-ms-buffers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$[?(@.code == 'FORMIC_ACID_1M')]").isNotEmpty())
                .andReturn();

        JsonNode buffers = objectMapper.readTree(result.getResponse().getContentAsString());
        int previousGroup = -1;
        String previousCodeInGroup = null;

        for (JsonNode buffer : buffers) {
            String code = buffer.path("code").asText("");
            int currentGroup = bufferGroup(code);

            assertTrue(currentGroup >= previousGroup, "Buffer group order should be non-decreasing");
            if (currentGroup == previousGroup && previousCodeInGroup != null) {
                assertTrue(code.compareTo(previousCodeInGroup) >= 0,
                        "Buffers in the same group should be alphabetical");
            }

            previousGroup = currentGroup;
            previousCodeInGroup = code;
        }
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

    private int bufferGroup(String code) {
        if (code.startsWith("FORMIC_ACID_")) {
            return 0;
        }
        if (code.startsWith("AMMONIUM_ACETATE_")) {
            return 1;
        }
        if (code.startsWith("AMMONIUM_BICARBONATE_")) {
            return 2;
        }
        if (code.startsWith("ACETIC_ACID_")) {
            return 3;
        }
        return 4;
    }
}
