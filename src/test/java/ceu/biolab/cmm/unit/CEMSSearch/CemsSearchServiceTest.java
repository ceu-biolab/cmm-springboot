package ceu.biolab.cmm.unit.CEMSSearch;

import ceu.biolab.cmm.CEMSSearch.dto.CeAnnotationsByAdductDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CeAnnotationDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsFeatureQueryDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsQueryResponseDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchRequestDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchResponseDTO;
import ceu.biolab.cmm.CEMSSearch.repository.CemsSearchRepository;
import ceu.biolab.cmm.CEMSSearch.service.CemsSearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CemsSearchServiceTest {

    @Mock
    private CemsSearchRepository repository;

    @InjectMocks
    private CemsSearchService service;

    @Captor
    private ArgumentCaptor<CemsFeatureQueryDTO> queryCaptor;

    private CemsSearchRequestDTO baseRequest() {
        CemsSearchRequestDTO request = new CemsSearchRequestDTO();
        request.setBufferCode("FORMIC_ACID_0DOT1M");
        request.setPolarity("Reverse");
        request.setIonizationMode("Negative");
        request.setChemicalAlphabet("ALL");
        request.setAdducts(List.of("M-H"));
        request.setMzValues(List.of(100.0));
        request.setEffectiveMobilities(List.of(1500.0));
        request.setMzToleranceMode("ppm");
        request.setMzTolerance(10.0);
        request.setEffectiveMobilityTolerance(5.0);
        request.setTemperature(20d);
        return request;
    }

    private CemsQueryResponseDTO candidate(long compoundId, String formula, double mass, double effMob) {
        CemsQueryResponseDTO dto = new CemsQueryResponseDTO();
        dto.setCompoundId(compoundId);
        dto.setCompoundName("Compound " + compoundId);
        dto.setFormula(formula);
        dto.setMass(mass);
        dto.setChargeNumber(1L);
        dto.setChargeType(1L);
        dto.setExperimentalEffMob(effMob);
        dto.setMobility(effMob);
        dto.setIonizationModeId(1);
        dto.setPolarityId(1);
        dto.setBufferCode("FORMIC_ACID_0DOT1M");
        dto.setFormulaType("CHNOPS");
        dto.setCompoundType(0);
        return dto;
    }

    @Test
    void searchThrowsWhenBufferCodeMissing() {
        CemsSearchRequestDTO request = baseRequest();
        request.setBufferCode(" ");

        assertThrows(IllegalArgumentException.class, () -> service.search(request));
    }

    @Test
    void searchThrowsWhenFeatureSizesDoNotMatch() {
        CemsSearchRequestDTO request = baseRequest();
        request.setEffectiveMobilities(List.of(1500.0, 1600.0));

        assertThrows(IllegalArgumentException.class, () -> service.search(request));
    }

    @Test
    void searchThrowsWhenTemperatureMissing() {
        CemsSearchRequestDTO request = baseRequest();
        request.setTemperature(null);

        assertThrows(IllegalArgumentException.class, () -> service.search(request));
    }

    @Test
    void searchReturnsSingleAnnotationPerCompound() throws Exception {
        CemsSearchRequestDTO request = baseRequest();

        CemsQueryResponseDTO c1 = candidate(1, "C5H11NO2", 117.078979, 1500.0);
        CemsQueryResponseDTO c1Alt = candidate(1, "C5H11NO2", 117.078989, 1501.0);
        CemsQueryResponseDTO c2 = candidate(2, "C6H12O6", 180.06339, 1502.0);

        when(repository.findMatchingCompounds(any(CemsFeatureQueryDTO.class)))
                .thenReturn(List.of(c1, c1Alt, c2));

        CemsSearchResponseDTO response = service.search(request);

        assertEquals(1, response.getCeFeatures().size());
        List<CeAnnotationsByAdductDTO> annotationsByAdduct = response.getCeFeatures().get(0).getAnnotationsByAdducts();
        assertEquals(1, annotationsByAdduct.size());

        List<CeAnnotationDTO> annotations = annotationsByAdduct.get(0).getAnnotations();
        assertEquals(2, annotations.size(), "Expected unique annotation per compound");
        long distinctIds = annotations.stream().map(a -> a.getCompound().getCompoundId()).distinct().count();
        assertEquals(distinctIds, annotations.size(), "Must remain unique per compound");
    }

    @Test
    void searchFiltersOutCompoundsOutsideChemicalAlphabet() throws Exception {
        CemsSearchRequestDTO request = baseRequest();
        request.setChemicalAlphabet("CHNOPS");

        CemsQueryResponseDTO allowed = candidate(1, "C6H12O6", 180.06339, 1500.0);
        CemsQueryResponseDTO rejected = candidate(2, "C6H12ClO6", 214.0249, 1500.0);

        when(repository.findMatchingCompounds(any(CemsFeatureQueryDTO.class)))
                .thenReturn(List.of(allowed, rejected));

        CemsSearchResponseDTO response = service.search(request);
        List<CeAnnotationsByAdductDTO> annotationsByAdduct = response.getCeFeatures().get(0).getAnnotationsByAdducts();
        List<CeAnnotationDTO> annotations = annotationsByAdduct.get(0).getAnnotations();

        assertEquals(1, annotations.size(), "Only compounds matching alphabet should survive");
        assertEquals(1, annotations.get(0).getCompound().getCompoundId());
    }

    @Test
    void searchIncludesCompoundsWithoutFormulaWhenAlphabetSpecified() throws Exception {
        CemsSearchRequestDTO request = baseRequest();
        request.setChemicalAlphabet("CHNOPS");

        CemsQueryResponseDTO noFormula = candidate(3, null, 150.0, 1500.0);
        CemsQueryResponseDTO allowed = candidate(4, "C2H4", 28.0313, 1500.0);

        when(repository.findMatchingCompounds(any(CemsFeatureQueryDTO.class)))
                .thenReturn(List.of(noFormula, allowed));

        CemsSearchResponseDTO response = service.search(request);
        List<CeAnnotationDTO> annotations = response.getCeFeatures()
                .get(0)
                .getAnnotationsByAdducts()
                .get(0)
                .getAnnotations();

        assertEquals(2, annotations.size(), "Compounds without formula should not be filtered out");
        assertTrue(annotations.stream().anyMatch(a -> a.getCompound().getCompoundId() == 3));
    }

    @Test
    void searchUsesAbsoluteMobilityToleranceWhenConfigured() throws Exception {
        CemsSearchRequestDTO request = baseRequest();
        request.setEffMobToleranceMode("absolute");
        request.setEffectiveMobilityTolerance(25.0);

        when(repository.findMatchingCompounds(any(CemsFeatureQueryDTO.class)))
                .thenReturn(List.of(candidate(5, "C3H6O3", 90.0, 1500.0)));

        service.search(request);

        verify(repository).findMatchingCompounds(queryCaptor.capture());
        CemsFeatureQueryDTO query = queryCaptor.getValue();

        assertEquals(1500.0 - 25.0, query.getMobilityLower(), 1e-9);
        assertEquals(1500.0 + 25.0, query.getMobilityUpper(), 1e-9);
    }
}
