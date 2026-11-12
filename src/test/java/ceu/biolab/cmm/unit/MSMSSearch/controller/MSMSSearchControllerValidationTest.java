package ceu.biolab.cmm.unit.MSMSSearch.controller;

import ceu.biolab.cmm.MSMSSearch.controller.MSMSSearchController;
import ceu.biolab.cmm.MSMSSearch.domain.CIDEnergy;
import ceu.biolab.cmm.MSMSSearch.domain.SpectrumSource;
import ceu.biolab.cmm.MSMSSearch.dto.LCMSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMSSearch.dto.LCMSMSSearchResponseDTO;
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

        mockMvc.perform(post("/api/msms-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayloadWithCidEnergy("ALL")))
                .andExpect(status().isOk());

        ArgumentCaptor<MSMSSearchRequestDTO> captor = ArgumentCaptor.forClass(MSMSSearchRequestDTO.class);
        verify(msmsSearchService).search(captor.capture());
        assertEquals(CIDEnergy.ALL, captor.getValue().getCIDEnergy());
        assertEquals(SpectrumSource.ALL, captor.getValue().getSpectrumSource());
    }

    @Test
    void search_rejectsMissingCidEnergy() throws Exception {
        mockMvc.perform(post("/api/msms-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayloadWithoutCidEnergy()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(msmsSearchService);
    }

    @Test
    void search_rejectsUnknownCidEnergy() throws Exception {
        mockMvc.perform(post("/api/msms-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayloadWithCidEnergy("UNKNOWN")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(msmsSearchService);
    }

    @Test
    void search_rejectsLowerCamelCaseCidEnergyField() throws Exception {
        mockMvc.perform(post("/api/msms-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadWithCustomCidEnergyField("cideEnergy", "ALL")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(msmsSearchService);
    }

    @Test
    void search_acceptsLowercaseSpectrumSource() throws Exception {
        when(msmsSearchService.search(any())).thenReturn(new MSMSSearchResponseDTO(new ArrayList<>()));

        mockMvc.perform(post("/api/msms-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadWithCustomSpectrumSource("predicted")))
                .andExpect(status().isOk());

        ArgumentCaptor<MSMSSearchRequestDTO> captor = ArgumentCaptor.forClass(MSMSSearchRequestDTO.class);
        verify(msmsSearchService).search(captor.capture());
        assertEquals(SpectrumSource.PREDICTED, captor.getValue().getSpectrumSource());
    }

    @Test
    void search_rejectsUnknownSpectrumSource() throws Exception {
        mockMvc.perform(post("/api/msms-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadWithCustomSpectrumSource("invented")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(msmsSearchService);
    }

    @Test
    void searchWithLcmsScoring_rejectsMissingRtValue() throws Exception {
        mockMvc.perform(post("/api/lcmsms-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayloadWithoutRtValue()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(msmsSearchService);
    }

    @Test
    void searchWithLcmsScoring_acceptsValidPayload() throws Exception {
        when(msmsSearchService.searchWithLcmsScoring(any(LCMSMSSearchRequestDTO.class)))
                .thenReturn(new LCMSMSSearchResponseDTO());

        mockMvc.perform(post("/api/lcmsms-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayloadWithRtValue()))
                .andExpect(status().isOk());

        verify(msmsSearchService).searchWithLcmsScoring(any(LCMSMSSearchRequestDTO.class));
    }

    @Test
    void searchWithLcmsScoring_acceptsBatchedPayload() throws Exception {
        when(msmsSearchService.searchWithLcmsScoring(any(LCMSMSSearchRequestDTO.class)))
                .thenReturn(new LCMSMSSearchResponseDTO());

        mockMvc.perform(post("/api/lcmsms-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayloadWithRtValues()))
                .andExpect(status().isOk());

        verify(msmsSearchService).searchWithLcmsScoring(any(LCMSMSSearchRequestDTO.class));
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
                  "scoreType": "COSINE",
                  "spectrumSource": "ALL"
                }
                """.formatted(cidEnergyField, cidEnergy);
    }

    private String payloadWithCustomSpectrumSource(String spectrumSource) {
        return """
                {
                  "CIDEnergy": "ALL",
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
                  "scoreType": "COSINE",
                  "spectrumSource": "%s"
                }
                """.formatted(spectrumSource);
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
                  "scoreType": "COSINE",
                  "spectrumSource": "ALL"
                }
                """;
    }

    private String validPayloadWithRtValue() {
        return """
                {
                  "CIDEnergy": "ALL",
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
                  "scoreType": "COSINE",
                  "spectrumSource": "ALL",
                  "rtValue": 5.2
                }
                """;
    }

    private String validPayloadWithoutRtValue() {
        return """
                {
                  "CIDEnergy": "ALL",
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
                  "scoreType": "COSINE",
                  "spectrumSource": "ALL"
                }
                """;
    }

    private String validPayloadWithRtValues() {
        return """
                {
                  "CIDEnergy": "ALL",
                  "tolerancePrecursorIon": 10.0,
                  "toleranceModePrecursorIon": "PPM",
                  "toleranceFragments": 30.0,
                  "toleranceModeFragments": "PPM",
                  "ionizationMode": "POSITIVE",
                  "adducts": ["[M+H]+"],
                  "precursorIonMZValues": [287.236, 305.247],
                  "fragmentsMZsIntensitiesList": [
                    {
                      "precursorMz": 287.236,
                      "peaks": [
                        { "mz": 121.035, "intensity": 100.0 }
                      ]
                    },
                    {
                      "precursorMz": 305.247,
                      "peaks": [
                        { "mz": 143.05, "intensity": 100.0 }
                      ]
                    }
                  ],
                  "scoreType": "COSINE",
                  "spectrumSource": "ALL",
                  "rtValues": [5.2, 6.1]
                }
                """;
    }
}
