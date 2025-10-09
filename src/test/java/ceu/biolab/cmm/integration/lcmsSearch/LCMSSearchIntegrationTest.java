package ceu.biolab.cmm.integration.lcmsSearch;

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
public class LCMSSearchIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    private String loadJson(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    @Test
    void testMSSearchSimpleSearchEndpoint1() throws Exception {
        String requestJson = loadJson("json/lcmsSearch/requestLCMSSearch.json");
        String expectedResponse = loadJson("json/lcmsSearch/responseLCMSSearch.json");

        mockMvc.perform(post("/api/batch-advanced-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse, JsonCompareMode.LENIENT));
    }
}
