package ceu.biolab.cmm.unit.CEMSMarkers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ceu.biolab.cmm.CEMSMarkers.domain.MarkerMobility;
import ceu.biolab.cmm.CEMSMarkers.dto.CemsRmtMarkersRequestDTO;
import ceu.biolab.cmm.CEMSMarkers.repository.CemsMarkersRepository;
import ceu.biolab.cmm.CEMSMarkers.service.CemsRmt1MarkerService;
import ceu.biolab.cmm.CEMSSearch.domain.CePolarity;
import ceu.biolab.cmm.CEMSSearch.domain.EffMobToleranceMode;
import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchRequestDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchResponseDTO;
import ceu.biolab.cmm.CEMSSearch.service.CemsSearchService;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CemsRmt1MarkerServiceTest {

    @Mock
    private CemsMarkersRepository markersRepository;

    @Mock
    private CemsSearchService cemsSearchService;

    @InjectMocks
    private CemsRmt1MarkerService service;

    @Captor
    private ArgumentCaptor<CemsSearchRequestDTO> requestCaptor;

    private CemsRmtMarkersRequestDTO sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = new CemsRmtMarkersRequestDTO();
        sampleRequest.setMasses(List.of(291.1299, 298.098, 308.094, 316.2488, 55.055));
        sampleRequest.setTolerance(10);
        sampleRequest.setToleranceMode("ppm");
        sampleRequest.setRelativeMigrationTimes(List.of(0.85, 0.86, 1.07, 0.93, 0.42));
        sampleRequest.setRmtTolerance(10);
        sampleRequest.setRmtToleranceMode("percentage");
        sampleRequest.setBuffer("FORMIC_ACID_1M");
        sampleRequest.setTemperature(20d);
        sampleRequest.setPolarity("Direct");
        sampleRequest.setRmtReference("L-Methionine sulfone");
        sampleRequest.setMarker("L-Methionine sulfone");
        sampleRequest.setMarkerTime(14.24);
        sampleRequest.setCapillaryLength(1000);
        sampleRequest.setCapillaryVoltage(30);
        sampleRequest.setChemicalAlphabet("CHNOPS");
        sampleRequest.setIonMode("positive");
        sampleRequest.setAdducts(List.of("[M+H]+", "[M+Na]+"));
    }

    @Test
    void searchComputesMobilitiesFromRmtAndDelegatesToCemsSearch() {
        when(markersRepository.findMarkerMobility(any(), any(), anyDouble(), any(CePolarity.class)))
                .thenReturn(Optional.of(new MarkerMobility(774.7394, "FORMIC_ACID_1M", CePolarity.DIRECT)))
                .thenReturn(Optional.of(new MarkerMobility(774.7394, "FORMIC_ACID_1M", CePolarity.DIRECT)));
        CemsSearchResponseDTO expectedResponse = new CemsSearchResponseDTO();
        when(cemsSearchService.search(any(CemsSearchRequestDTO.class))).thenReturn(expectedResponse);

        CemsSearchResponseDTO response = service.search(sampleRequest);

        assertEquals(expectedResponse, response);

        verify(cemsSearchService).search(requestCaptor.capture());
        CemsSearchRequestDTO forwarded = requestCaptor.getValue();

        double[] expectedMobilities = {
                1187.825982947786,
                1155.8037672153994,
                621.6013832685774,
                950.9304515081951,
                4007.3058348136265
        };
        for (int i = 0; i < expectedMobilities.length; i++) {
            assertEquals(expectedMobilities[i], forwarded.getEffectiveMobilities().get(i), 1e-9,
                    "Effective mobility mismatch at index " + i);
        }

        assertEquals(39.104838498883545, forwarded.getEffectiveMobilityTolerance(), 1e-9);
        assertEquals(sampleRequest.getMasses(), forwarded.getMzValues());
        assertEquals(sampleRequest.getAdducts(), forwarded.getAdducts());
        assertEquals("FORMIC_ACID_1M", forwarded.getBufferCode());
        assertEquals(10d, forwarded.getMzTolerance());
        assertEquals(MzToleranceMode.PPM, forwarded.getMzToleranceMode());
        assertEquals(sampleRequest.getTemperature(), forwarded.getTemperature());
        assertEquals(EffMobToleranceMode.PERCENTAGE, forwarded.getEffectiveMobilityToleranceMode());
    }

    @Test
    void searchThrowsWhenRmtReferenceMobilityMissing() {
        when(markersRepository.findMarkerMobility(any(), any(), anyDouble(), any(CePolarity.class)))
                .thenReturn(Optional.of(new MarkerMobility(774.7394, "FORMIC_ACID_1M", CePolarity.DIRECT)))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.search(sampleRequest));
    }

    @Test
    void searchRejectsNeutralIonizationMode() {
        sampleRequest.setIonMode("neutral");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.search(sampleRequest));
        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}
