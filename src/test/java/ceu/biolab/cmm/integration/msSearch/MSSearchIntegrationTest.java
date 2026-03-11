package ceu.biolab.cmm.integration.msSearch;

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

import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MSSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String loadJson(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    @Test
    void testMSSearchSimpleSearchEndpoint1() throws Exception {
        String requestJson = loadJson("json/msSearch/requestMSSearchSimple1.json");
        String expectedResponse = loadJson("json/msSearch/responseMSSearchSimple1.json");

        MvcResult result = mockMvc.perform(post("/api/compounds/simple-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();
        String actual = result.getResponse().getContentAsString();
        JSONAssert.assertEquals(expectedResponse, actual, JSONCompareMode.STRICT);
    }

    @Test
    void testMSSearchBatchSearchEndpoint1() throws Exception {
        String requestJson = loadJson("json/msSearch/requestMSSearchBatch1.json");
        String expectedResponse = loadJson("json/msSearch/responseMSSearchBatch1.json");

        MvcResult result = mockMvc.perform(post("/api/compounds/batch-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();
        String actual = result.getResponse().getContentAsString();
        JSONAssert.assertEquals(expectedResponse, actual, JSONCompareMode.STRICT);
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
}