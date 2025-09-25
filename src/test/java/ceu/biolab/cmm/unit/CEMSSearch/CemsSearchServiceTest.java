package ceu.biolab.cmm.unit.CEMSSearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ceu.biolab.cmm.CEMSSearch.dto.CeAnnotationsByAdductDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CeFeatureAnnotationsDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CeAnnotationDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchRequestDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchResponseDTO;
import ceu.biolab.cmm.CEMSSearch.repository.CemsSearchRepository;
import ceu.biolab.cmm.CEMSSearch.service.CemsSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.util.List;

@SpringBootTest
@ActiveProfiles("local")
class CemsSearchServiceTest {

    @Autowired
    private DataSource dataSource;

    private CemsSearchService cemsSearchService;

    @BeforeEach
    void setUp() {
        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        CemsSearchRepository repository = new CemsSearchRepository(jdbcTemplate, new DefaultResourceLoader());
        cemsSearchService = new CemsSearchService(repository);
    }

    @Test
    void searchThrowsForUnknownBackgroundElectrolyte() {
        CemsSearchRequestDTO request = new CemsSearchRequestDTO();
        request.setBackgroundElectrolyte("not-a-known-bge");
        request.setPolarity("Direct");
        request.setIonizationMode("Positive");
        request.setAdducts(List.of("M+H"));
        request.setMzValues(List.of(100.0));
        request.setEffectiveMobilities(List.of(1000.0));
        request.setMzToleranceMode("ppm");
        request.setMzTolerance(10.0);
        request.setEffectiveMobilityTolerance(5.0);

        assertThrows(IllegalArgumentException.class, () -> cemsSearchService.search(request));
    }

    @Test
    void searchThrowsWhenFeatureSizesDoNotMatch() {
        CemsSearchRequestDTO request = new CemsSearchRequestDTO();
        request.setBackgroundElectrolyte("formic acid 1M");
        request.setPolarity("Direct");
        request.setIonizationMode("Positive");
        request.setAdducts(List.of("M+H"));
        request.setMzValues(List.of(100.0));
        request.setEffectiveMobilities(List.of(1000.0, 2000.0));
        request.setMzToleranceMode("ppm");
        request.setMzTolerance(10.0);
        request.setEffectiveMobilityTolerance(5.0);

        assertThrows(IllegalArgumentException.class, () -> cemsSearchService.search(request));
    }

    @Test
    void searchReturnsSingleAnnotationPerCompound() {
        CemsSearchRequestDTO request = new CemsSearchRequestDTO();
        request.setBackgroundElectrolyte("formic acid 0.1M");
        request.setPolarity("Reverse");
        request.setIonizationMode("Negative");
        request.setChemicalAlphabet("CHNOPS");
        request.setInputMassMode("m/z");
        request.setAdducts(List.of("M-H"));
        request.setMzValues(List.of(115.003658616));
        request.setEffectiveMobilities(List.of(1699.0335686356439));
        request.setMzToleranceMode("ppm");
        request.setMzTolerance(30.0);
        request.setEffectiveMobilityTolerance(10.0);

        CemsSearchResponseDTO response = cemsSearchService.search(request);

        assertEquals(1, response.getCeFeatures().size(), "One feature expected");
        List<CeFeatureAnnotationsDTO> features = response.getCeFeatures();
        List<CeAnnotationsByAdductDTO> annotationsByAdduct = features.get(0).getAnnotationsByAdducts();
        assertEquals(1, annotationsByAdduct.size(), "Single adduct should be returned");

        List<CeAnnotationDTO> annotations = annotationsByAdduct.get(0).getAnnotations();

        long distinctCompoundIds = annotations
                .stream()
                .map(annotation -> annotation.getCompound().getCompoundId())
                .distinct()
                .count();

        assertEquals(distinctCompoundIds, annotations.size(), "Annotations must be unique per compound");
        assertTrue(annotations.size() > 0, "Expected at least one annotation for the feature");
    }
}
