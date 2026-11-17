package ceu.biolab.cmm.unit.CEMSMarkers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ceu.biolab.cmm.CEMSMarkers.domain.MarkerMobility;
import ceu.biolab.cmm.CEMSMarkers.dto.CemsMarkersRequestDTO;
import ceu.biolab.cmm.CEMSMarkers.repository.CemsMarkersRepository;
import ceu.biolab.cmm.CEMSMarkers.service.Cems1MarkerService;
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
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class Cems1MarkerServiceTest {

    @Mock
    private CemsMarkersRepository markersRepository;

    @Mock
    private CemsSearchService cemsSearchService;

    @InjectMocks
    private Cems1MarkerService service;

    @Captor
    private ArgumentCaptor<CemsSearchRequestDTO> requestCaptor;

    private CemsMarkersRequestDTO sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = new CemsMarkersRequestDTO();
        sampleRequest.setMasses(List.of(291.1299, 298.098, 308.094, 316.2488, 55.055));
        sampleRequest.setTolerance(10);
        sampleRequest.setToleranceMode("mDa");
        sampleRequest.setMigrationTimes(List.of(11.56, 13.65, 15.62, 12.59, 6.99));
        sampleRequest.setMigrationTimeTolerance(10);
        sampleRequest.setMtToleranceMode("percentage");
        sampleRequest.setBuffer("FORMIC_ACID_1M");
        sampleRequest.setTemperature(20d);
        sampleRequest.setPolarity("Direct");
        sampleRequest.setMarker("L-Methionine sulfone");
        sampleRequest.setMarkerTime(14.24);
        sampleRequest.setCapillaryLength(1000);
        sampleRequest.setCapillaryVoltage(30);
        sampleRequest.setChemicalAlphabet("CHNOPS");
        sampleRequest.setIonMode("positive");
        sampleRequest.setAdducts(List.of("[M+H]+", "[M+Na]+"));
    }

    @Test
    void searchComputesMobilitiesAndDelegatesToCemsSearch() {
        when(markersRepository.findMarkerMobility(any(), any(), anyDouble(), any(CePolarity.class)))
                .thenReturn(Optional.of(new MarkerMobility(774.7394, "FORMIC_ACID_1M", CePolarity.DIRECT)));
        CemsSearchResponseDTO expectedResponse = new CemsSearchResponseDTO();
        when(cemsSearchService.search(any(CemsSearchRequestDTO.class))).thenReturn(expectedResponse);

        CemsSearchResponseDTO response = service.search(sampleRequest);

        assertEquals(expectedResponse, response);

        verify(cemsSearchService).search(requestCaptor.capture());
        CemsSearchRequestDTO forwarded = requestCaptor.getValue();

        double[] expectedMobilities = {
                1317.4217736765027,
                875.9178719649888,
                567.9316484858075,
                1081.519348416346,
                3202.632645031693
        };
        for (int i = 0; i < expectedMobilities.length; i++) {
            assertEquals(expectedMobilities[i], forwarded.getEffectiveMobilities().get(i), 1e-9,
                    "Effective mobility mismatch at index " + i);
        }

        assertEquals(forwarded.getMzValues().size(), forwarded.getMobilityBounds().size());
        for (int i = 0; i < sampleRequest.getMigrationTimes().size(); i++) {
            double time = sampleRequest.getMigrationTimes().get(i);
            double factor = sampleRequest.getCapillaryLength() * sampleRequest.getCapillaryLength()
                    / sampleRequest.getCapillaryVoltage();
            double lower = 774.7394 + factor * (1 / (time * 1.1) - 1 / sampleRequest.getMarkerTime());
            double upper = 774.7394 + factor * (1 / (time * 0.9) - 1 / sampleRequest.getMarkerTime());
            assertEquals(lower, forwarded.getMobilityBounds().get(i).lower(), 1e-8);
            assertEquals(upper, forwarded.getMobilityBounds().get(i).upper(), 1e-8);
        }
        for (int i = 0; i < forwarded.getMobilityBounds().size(); i++) {
            org.junit.jupiter.api.Assertions.assertTrue(forwarded.getMobilityBounds().get(i).lower()
                    < forwarded.getEffectiveMobilities().get(i));
            org.junit.jupiter.api.Assertions.assertTrue(forwarded.getMobilityBounds().get(i).upper()
                    > forwarded.getEffectiveMobilities().get(i));
        }
        assertEquals(sampleRequest.getMasses(), forwarded.getMzValues());
        assertEquals(sampleRequest.getAdducts(), forwarded.getAdducts());
        assertEquals("FORMIC_ACID_1M", forwarded.getBufferCode());
        assertEquals(10d, forwarded.getMzTolerance());
        assertEquals(MzToleranceMode.MDA, forwarded.getMzToleranceMode());
        assertEquals(sampleRequest.getTemperature(), forwarded.getTemperature());
        assertEquals(ceu.biolab.cmm.CEMSSearch.domain.EffMobToleranceMode.ABSOLUTE,
                forwarded.getEffectiveMobilityToleranceMode());
    }

    @Test
    void searchThrowsWhenMarkerMobilityMissing() {
        when(markersRepository.findMarkerMobility(any(), any(), anyDouble(), any(CePolarity.class)))
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
