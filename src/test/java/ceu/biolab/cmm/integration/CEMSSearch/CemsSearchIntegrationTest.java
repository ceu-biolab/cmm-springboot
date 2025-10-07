package ceu.biolab.cmm.integration.CEMSSearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ceu.biolab.cmm.CEMSSearch.dto.CeAnnotationDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CeFeatureAnnotationsDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class CemsSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void cemsEndpointReturnsUniqueCompoundsPerFeature() throws Exception {
        String requestJson = """
                {
                  \"buffer_code\": \"FORMIC_ACID_0DOT1M\",
                  \"polarity\": \"Reverse\",
                  \"chemical_alphabet\": \"CHNOPS\",
                  \"input_mass_mode\": \"m/z\",
                  \"ionization_mode\": \"Negative\",
                  \"adducts\": [\"M-H\"],
                  \"mz_values\": [115.003658616],
                  \"effective_mobilities\": [1699.0335686356439],
                  \"mz_tolerance\": 30.0,
                  \"mz_tolerance_mode\": \"ppm\",
                  \"eff_mob_tolerance\": 10.0
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/CEMSSearch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        CemsSearchResponseDTO response = objectMapper.readValue(responseBody, CemsSearchResponseDTO.class);

        List<CeFeatureAnnotationsDTO> features = response.getCeFeatures();
        assertEquals(1, features.size(), "Expected single feature in response");

        assertEquals(1, features.get(0).getAnnotationsByAdducts().size(), "Expected single adduct block");

        List<CeAnnotationDTO> annotations = features.get(0)
                .getAnnotationsByAdducts()
                .get(0)
                .getAnnotations();

        assertFalse(annotations.isEmpty(), "Should have at least one annotation");

        long uniqueIds = annotations.stream()
                .map(annotation -> annotation.getCompound().getCompoundId())
                .collect(Collectors.toSet())
                .size();

        assertEquals(uniqueIds, annotations.size(), "Annotations should be unique per compound");
    }
}
