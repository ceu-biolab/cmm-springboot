package ceu.biolab.cmm.integration.CEMSSearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.FileCopyUtils;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class CemsRmtSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void cemsRmtEndpointReturnsAnnotations() throws Exception {
        String requestJson = readResource("/json/cemsSearch/CEMSRMT_request1.json");
        String expectedJson = readResource("/json/cemsSearch/CEMSRMT_response1.json");

        MvcResult result = mockMvc.perform(post("/api/CEMSRMTSearch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode actualNode = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode expectedNode = objectMapper.readTree(expectedJson);

        assertEquals(expectedNode, actualNode, "Response payload must match expected snapshot");
    }

    private String readResource(String path) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + path);
            }
            byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }
}
