package ceu.biolab.cmm.unit.CEMSSearch;

import ceu.biolab.cmm.CEMSSearch.dto.CeAnnotationDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CeAnnotationsByAdductDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsQueryResponseDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsRmtFeatureQueryDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsRmtSearchRequestDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchResponseDTO;
import ceu.biolab.cmm.CEMSSearch.repository.CemsRmtSearchRepository;
import ceu.biolab.cmm.CEMSSearch.service.CemsRmtSearchService;
import java.util.List;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CemsRmtSearchServiceTest {

    @Mock
    private CemsRmtSearchRepository repository;

    @InjectMocks
    private CemsRmtSearchService service;

    @Captor
    private ArgumentCaptor<CemsRmtFeatureQueryDTO> queryCaptor;

    private CemsRmtSearchRequestDTO baseRequest() {
        CemsRmtSearchRequestDTO request = new CemsRmtSearchRequestDTO();
        request.setMasses(List.of(291.1299));
        request.setTolerance(10d);
        request.setToleranceMode("ppm");
        request.setRelativeMigrationTimes(List.of(0.85));
        request.setRmtTolerance(10d);
        request.setRmtToleranceMode("percentage");
        request.setBufferCode("FORMIC_ACID_1M");
        request.setTemperature(20d);
        request.setPolarity("Direct");
        request.setRmtReference("L-Methionine sulfone");
        request.setChemicalAlphabet("ALL");
        request.setIonMode("positive");
        request.setAdducts(List.of("[M+H]+"));
        return request;
    }

    private CemsQueryResponseDTO candidate(long compoundId, double mass, double rmt) {
        CemsQueryResponseDTO dto = new CemsQueryResponseDTO();
        dto.setCompoundId(compoundId);
        dto.setCompoundName("Compound " + compoundId);
        dto.setMass(mass);
        dto.setChargeNumber(1L);
        dto.setChargeType(1L);
        dto.setFormula("C6H12O6");
        dto.setFormulaType("CHNOPS");
        dto.setCompoundType(0);
        dto.setRelativeMt(rmt);
        dto.setAbsoluteMt(12.34);
        dto.setIonizationModeId(1);
        dto.setPolarityId(1);
        dto.setBufferCode("FORMIC_ACID_1M");
        return dto;
    }

    @Test
    void searchThrowsWhenReferenceUnknown() {
        CemsRmtSearchRequestDTO request = baseRequest();

        when(repository.findReferenceCompoundId(request.getRmtReference()))
                .thenReturn(OptionalLong.empty());

        assertThrows(ResponseStatusException.class, () -> service.search(request));
    }

    @Test
    void searchReturnsDeduplicatedAnnotations() throws Exception {
        CemsRmtSearchRequestDTO request = baseRequest();

        when(repository.findReferenceCompoundId(request.getRmtReference()))
                .thenReturn(OptionalLong.of(180838));

        CemsQueryResponseDTO better = candidate(1, 290.0, 0.86);
        CemsQueryResponseDTO worse = candidate(1, 295.0, 0.9);
        CemsQueryResponseDTO other = candidate(2, 310.0, 0.84);

        when(repository.findMatchingCompounds(any(CemsRmtFeatureQueryDTO.class)))
                .thenReturn(List.of(worse, better, other));

        CemsSearchResponseDTO response = service.search(request);

        verify(repository).findMatchingCompounds(queryCaptor.capture());
        CemsRmtFeatureQueryDTO query = queryCaptor.getValue();
        assertEquals("FORMIC_ACID_1M", query.getBufferCode());
        assertEquals(180838L, query.getReferenceCompoundId());

        assertEquals(1, response.getCeFeatures().size());
        List<CeAnnotationsByAdductDTO> annotationsByAdduct = response.getCeFeatures().get(0).getAnnotationsByAdducts();
        assertEquals(1, annotationsByAdduct.size());

        List<CeAnnotationDTO> annotations = annotationsByAdduct.get(0).getAnnotations();
        assertEquals(2, annotations.size());

        CeAnnotationDTO first = annotations.get(0);
        assertEquals(1, first.getCompound().getCompoundId());
        assertNotNull(first.getRmtErrorPct());
        assertEquals(0.86, first.getRelativeMt(), 1e-9);

        CeAnnotationDTO second = annotations.get(1);
        assertEquals(2, second.getCompound().getCompoundId());
    }

    @Test
    void searchThrowsWhenBufferMissing() {
        CemsRmtSearchRequestDTO request = baseRequest();
        request.setBufferCode(" ");

        assertThrows(ResponseStatusException.class, () -> service.search(request));
    }
}
