package ceu.biolab.cmm.unit.MSMSSearch.controller;

import ceu.biolab.cmm.MSMSSearch.controller.MSMSSearchController;
import ceu.biolab.cmm.MSMSSearch.domain.CIDEnergy;
import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchResponseDTO;
import ceu.biolab.cmm.MSMSSearch.service.MSMSSearchService;
import ceu.biolab.cmm.config.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MSMSSearchController.class)
@Import(GlobalExceptionHandler.class)
class MSMSSearchControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MSMSSearchService msmsSearchService;

    @Test
    void search_acceptsAllCidEnergy() throws Exception {
        when(msmsSearchService.search(any())).thenReturn(new MSMSSearchResponseDTO(new ArrayList<>()));

        mockMvc.perform(post("/api/MSMSSearch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayloadWithCidEnergy("ALL")))
                .andExpect(status().isOk());

        ArgumentCaptor<MSMSSearchRequestDTO> captor = ArgumentCaptor.forClass(MSMSSearchRequestDTO.class);
        verify(msmsSearchService).search(captor.capture());
        assertEquals(CIDEnergy.ALL, captor.getValue().getCIDEnergy());
    }

    @Test
    void search_rejectsMissingCidEnergy() throws Exception {
        mockMvc.perform(post("/api/MSMSSearch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayloadWithoutCidEnergy()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(msmsSearchService);
    }

    @Test
    void search_rejectsUnknownCidEnergy() throws Exception {
        mockMvc.perform(post("/api/MSMSSearch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayloadWithCidEnergy("UNKNOWN")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(msmsSearchService);
    }

    @Test
    void search_rejectsLowerCamelCaseCidEnergyField() throws Exception {
        mockMvc.perform(post("/api/MSMSSearch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadWithCustomCidEnergyField("cideEnergy", "ALL")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(msmsSearchService);
    }

    private String validPayloadWithCidEnergy(String cidEnergy) {
        return payloadWithCustomCidEnergyField("CIDEnergy", cidEnergy);
    }

    private String payloadWithCustomCidEnergyField(String cidEnergyField, String cidEnergy) {
        return """
                {
                  "%s": "%s",
                  "precursorIonMZ": 287.236,
                  "tolerancePrecursorIon": 10.0,
                  "toleranceModePrecursorIon": "PPM",
                  "toleranceFragments": 30.0,
                  "toleranceModeFragments": "PPM",
                  "ionizationMode": "POSITIVE",
                  "adducts": ["[M+H]+"],
                  "fragmentsMZsIntensities": {
                    "precursorMz": 287.236,
                    "peaks": [
                      { "mz": 121.035, "intensity": 100.0 }
                    ]
                  },
                  "scoreType": "COSINE"
                }
                """.formatted(cidEnergyField, cidEnergy);
    }

    private String validPayloadWithoutCidEnergy() {
        return """
                {
                  "precursorIonMZ": 287.236,
                  "tolerancePrecursorIon": 10.0,
                  "toleranceModePrecursorIon": "PPM",
                  "toleranceFragments": 30.0,
                  "toleranceModeFragments": "PPM",
                  "ionizationMode": "POSITIVE",
                  "adducts": ["[M+H]+"],
                  "fragmentsMZsIntensities": {
                    "precursorMz": 287.236,
                    "peaks": [
                      { "mz": 121.035, "intensity": 100.0 }
                    ]
                  },
                  "scoreType": "COSINE"
                }
                """;
    }
}
