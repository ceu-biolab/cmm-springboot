package ceu.biolab.cmm.adapters.batchAdvancedSearchAdapter;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BatchAdvancedSearchServiceTest {
    @Autowired
    private MockMvc mockMvc;

    private String loadJson(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    @Test
    void testBatchAdvancedSearchAdapterRequest1() throws Exception {
        String requestJson = loadJson("json/adapters/batchAdvancedSearchAdapter/request1.json");
        String expectedResponse = loadJson("json/adapters/batchAdvancedSearchAdapter/response1.json");
        mockMvc.perform(post("/api/advancedbatch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse));
    }
}
