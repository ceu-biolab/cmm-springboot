package ceu.biolab.cmm.unit.CEMSMarkers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ceu.biolab.cmm.CEMSMarkers.domain.MarkerMobility;
import ceu.biolab.cmm.CEMSMarkers.dto.CemsMarkersTwoRequestDTO;
import ceu.biolab.cmm.CEMSMarkers.repository.CemsMarkersRepository;
import ceu.biolab.cmm.CEMSMarkers.service.Cems2MarkerService;
import ceu.biolab.cmm.CEMSSearch.domain.CePolarity;
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

@ExtendWith(MockitoExtension.class)
class Cems2MarkerServiceTest {

    @Mock
    private CemsMarkersRepository markersRepository;

    @Mock
    private CemsSearchService cemsSearchService;

    @InjectMocks
    private Cems2MarkerService service;

    @Captor
    private ArgumentCaptor<CemsSearchRequestDTO> requestCaptor;

    private CemsMarkersTwoRequestDTO sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = new CemsMarkersTwoRequestDTO();
        sampleRequest.setMasses(List.of(291.1299, 298.098, 308.094, 316.2488, 55.055));
        sampleRequest.setTolerance(10);
        sampleRequest.setToleranceMode("mDa");
        sampleRequest.setMigrationTimes(List.of(11.56, 13.65, 15.62, 12.59, 6.99));
        sampleRequest.setMigrationTimeTolerance(10);
        sampleRequest.setMtToleranceMode("percentage");
        sampleRequest.setBuffer("FORMIC_ACID_1M");
        sampleRequest.setTemperature(20d);
        sampleRequest.setPolarity("Direct");
        sampleRequest.setMarker1("L-Methionine sulfone");
        sampleRequest.setMarker1Time(14.24);
        sampleRequest.setMarker2("Hippuric acid");
        sampleRequest.setMarker2Time(25.29);
        sampleRequest.setChemicalAlphabet("CHNOPS");
        sampleRequest.setIonMode("positive");
        sampleRequest.setMassMode("mz");
        sampleRequest.setAdducts(List.of("M+H", "M+Na"));
    }

    @Test
    void searchComputesMobilitiesAndDelegatesToCemsSearch() {
        when(markersRepository.findMarkerMobility(any(), any(), anyDouble(), any(CePolarity.class)))
                .thenReturn(
                        Optional.of(new MarkerMobility(774.7394, "FORMIC_ACID_1M", CePolarity.DIRECT)),
                        Optional.of(new MarkerMobility(-43.8585522259217, "FORMIC_ACID_1M", CePolarity.DIRECT))
                );
        CemsSearchResponseDTO expectedResponse = new CemsSearchResponseDTO();
        when(cemsSearchService.search(any(CemsSearchRequestDTO.class))).thenReturn(expectedResponse);

        CemsSearchResponseDTO response = service.search(sampleRequest);

        assertEquals(expectedResponse, response);

        verify(cemsSearchService).search(requestCaptor.capture());
        CemsSearchRequestDTO forwarded = requestCaptor.getValue();

        double[] expectedMobilities = {
                1209.083738627556,
                855.7191749487557,
                609.2175765327253,
                1020.2755372737782,
                2717.941790428516
        };
        for (int i = 0; i < expectedMobilities.length; i++) {
            assertEquals(expectedMobilities[i], forwarded.getEffectiveMobilities().get(i), 1e-9,
                    "Effective mobility mismatch at index " + i);
        }

        assertEquals(31.15094534757435, forwarded.getEffectiveMobilityTolerance(), 1e-9);
        assertEquals(sampleRequest.getMasses(), forwarded.getMzValues());
        assertEquals(sampleRequest.getAdducts(), forwarded.getAdducts());
        assertEquals("FORMIC_ACID_1M", forwarded.getBufferCode());
        assertEquals(10d, forwarded.getMzTolerance());
        assertEquals(MzToleranceMode.MDA, forwarded.getMzToleranceMode());
        assertEquals(sampleRequest.getTemperature(), forwarded.getTemperature());
        assertEquals(ceu.biolab.cmm.CEMSSearch.domain.EffMobToleranceMode.PERCENTAGE,
                forwarded.getEffectiveMobilityToleranceMode());
    }

    @Test
    void searchThrowsWhenMarkerMobilityMissing() {
        when(markersRepository.findMarkerMobility(any(), any(), anyDouble(), any(CePolarity.class)))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.search(sampleRequest));
    }
}
