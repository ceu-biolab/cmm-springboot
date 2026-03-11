package ceu.biolab.cmm.adapters.simpleSearchAdapter;

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
public class SimpleSearchAdapterServiceTest {
    @Autowired
    private MockMvc mockMvc;

    private String loadJson(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    @Test
    void testSimpleSearchAdapterRequest1() throws Exception {
        String requestJson = loadJson("json/adapters/simpleSearchAdapter/request1.json");
        String expectedResponse = loadJson("json/adapters/simpleSearchAdapter/response1.json");
        mockMvc.perform(post("/api/simplesearch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse));
    }

}
