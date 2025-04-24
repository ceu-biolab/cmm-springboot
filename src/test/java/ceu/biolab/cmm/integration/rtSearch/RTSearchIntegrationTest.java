package ceu.biolab.cmm.integration.rtSearch;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RTSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String loadJson(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    @Test
    void testRTSearchSimpleSearchEndpoint1() throws Exception {
        String requestJson = loadJson("json/rtSearch/requestRTSearchSimple1.json");
        String expectedResponse = loadJson("json/rtSearch/responseRTSearchSimple1.json");

        mockMvc.perform(post("/api/compounds/simple-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));
    }

    @Test
    void testRTSearchBatchSearchEndpoint1() throws Exception {
        String requestJson = loadJson("json/rtSearch/requestRTSearchBatch1.json");
        String expectedResponse = loadJson("json/rtSearch/responseRTSearchBatch1.json");

        mockMvc.perform(post("/api/compounds/batch-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse, JsonCompareMode.STRICT));
    }
}
