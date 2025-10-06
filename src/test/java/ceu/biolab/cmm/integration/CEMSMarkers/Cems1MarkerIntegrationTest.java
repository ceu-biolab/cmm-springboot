package ceu.biolab.cmm.integration.CEMSMarkers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class Cems1MarkerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void cemsMarkersEndpointMatchesStoredResponse() throws Exception {
        ClassPathResource requestResource = new ClassPathResource("json/cemsMarkers/CEMS1Marker_request1.json");
        ClassPathResource responseResource = new ClassPathResource("json/cemsMarkers/CEMS1Marker_response1.json");

        String requestJson = new String(requestResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        JsonNode expected = objectMapper.readTree(responseResource.getInputStream());

        MvcResult mvcResult = mockMvc.perform(post("/api/CEMS1Marker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode actual = objectMapper.readTree(mvcResult.getResponse().getContentAsString());

        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
}
