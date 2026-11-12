package ceu.biolab.cmm.unit.ccsSearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ceu.biolab.cmm.ccsSearch.domain.BufferGas;
import ceu.biolab.cmm.ccsSearch.domain.CcsToleranceMode;
import ceu.biolab.cmm.ccsSearch.dto.CcsFeatureQueryDTO;
import ceu.biolab.cmm.ccsSearch.dto.CcsQueryResponseDTO;
import ceu.biolab.cmm.ccsSearch.dto.CcsSearchRequestDTO;
import ceu.biolab.cmm.ccsSearch.dto.CcsSearchResponseDTO;
import ceu.biolab.cmm.ccsSearch.dto.CcsScoringRequestDTO;
import ceu.biolab.cmm.ccsSearch.repository.CcsSearchRepository;
import ceu.biolab.cmm.ccsSearch.service.CcsSearchService;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.msFeature.Annotation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CcsSearchServiceTest {

    @Mock
    private CcsSearchRepository repository;

    @InjectMocks
    private CcsSearchService service;

    @Test
    void searchSkipsCandidatesWithMissingChargeFields() throws Exception {
        CcsSearchRequestDTO request = baseRequest();

        CcsQueryResponseDTO missingCharge = candidate(10, null, 1);
        CcsQueryResponseDTO valid = candidate(11, 1, 1);

        when(repository.findMatchingCompounds(any(CcsFeatureQueryDTO.class)))
                .thenReturn(List.of(missingCharge, valid));

        CcsSearchResponseDTO response = service.search(request);
        List<Annotation> annotations = response.getImFeatures()
                .get(0)
                .getAnnotationsByAdducts()
                .get(0)
                .getAnnotations();

        assertEquals(1, annotations.size());
        assertEquals(11, annotations.get(0).getCompound().getCompoundId());
    }

    @Test
    void searchUsesCompositeSpectrumToPrioritizeDetectedAdduct() throws Exception {
        CcsScoringRequestDTO request = baseScoringRequest();
        request.setMzValues(List.of(200.0));
        request.setAdducts(List.of("[M+Na]+", "[M+H]+"));
        Map<Double, Double> compositeSpectrum = new LinkedHashMap<>();
        compositeSpectrum.put(200.0, 50.0);
        compositeSpectrum.put(221.9819, 100.0);
        request.setCompositeSpectrum(List.of(compositeSpectrum));

        when(repository.findMatchingCompounds(any(CcsFeatureQueryDTO.class)))
                .thenReturn(List.of());

        service.search(request);

        ArgumentCaptor<CcsFeatureQueryDTO> captor = ArgumentCaptor.forClass(CcsFeatureQueryDTO.class);
        verify(repository, times(2)).findMatchingCompounds(captor.capture());
        List<CcsFeatureQueryDTO> queries = captor.getAllValues();
        assertEquals("M+H", queries.get(0).getAdduct());
        assertEquals("M+Na", queries.get(1).getAdduct());
    }

    @Test
    void searchRejectsCompositeSpectrumSizeMismatch() {
        CcsScoringRequestDTO request = baseScoringRequest();
        request.setMzValues(List.of(200.0, 201.0));
        request.setCcsValues(List.of(150.0, 151.0));
        request.setCompositeSpectrum(List.of(Map.of(200.0, 100.0)));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.search(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    private CcsSearchRequestDTO baseRequest() {
        CcsSearchRequestDTO request = new CcsSearchRequestDTO();
        request.setMzValues(List.of(100.0));
        request.setMzTolerance(10.0);
        request.setMzToleranceMode(MzToleranceMode.PPM);
        request.setCcsValues(List.of(150.0));
        request.setCcsTolerance(2.0);
        request.setCcsToleranceMode(CcsToleranceMode.ABSOLUTE);
        request.setIonizationMode(IonizationMode.POSITIVE);
        request.setBufferGas(BufferGas.N2);
        request.setAdducts(List.of("[M+H]+"));
        return request;
    }

    private CcsScoringRequestDTO baseScoringRequest() {
        CcsScoringRequestDTO request = new CcsScoringRequestDTO();
        request.setMzValues(List.of(100.0));
        request.setMzTolerance(10.0);
        request.setMzToleranceMode(MzToleranceMode.PPM);
        request.setCcsValues(List.of(150.0));
        request.setCcsTolerance(2.0);
        request.setCcsToleranceMode(CcsToleranceMode.ABSOLUTE);
        request.setIonizationMode(IonizationMode.POSITIVE);
        request.setBufferGas(BufferGas.N2);
        request.setAdducts(List.of("[M+H]+"));
        request.setRtValues(List.of(5.0));
        return request;
    }

    private CcsQueryResponseDTO candidate(int compoundId, Integer chargeType, Integer chargeNumber) {
        CcsQueryResponseDTO dto = new CcsQueryResponseDTO();
        dto.setCompoundId(compoundId);
        dto.setCompoundName("Compound " + compoundId);
        dto.setFormula("C6H12O6");
        dto.setMonoisotopicMass(180.06339);
        dto.setChargeType(chargeType);
        dto.setChargeNumber(chargeNumber);
        dto.setCompoundType(0);
        dto.setDbCcs(150.0);
        dto.setPathwayId(1);
        dto.setPathwayName("Pathway");
        dto.setPathwayMap("map00010");
        return dto;
    }
}
