package ceu.biolab.cmm.unit.legacyApi;

import ceu.biolab.cmm.legacyApi.dto.*;
import ceu.biolab.cmm.legacyApi.mapper.*;
import ceu.biolab.cmm.legacyApi.service.*;
import ceu.biolab.cmm.msSearch.service.CompoundService;
import ceu.biolab.cmm.msSearch.repository.CompoundRepository;
import ceu.biolab.cmm.MSMSSearch.service.MSMSSearchService;
import ceu.biolab.cmm.MSMSSearch.repository.MSMSSearchRepository;
import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchResponseDTO;
import ceu.biolab.cmm.lcmsSearch.service.BatchAdvancedSearchService;
import ceu.biolab.cmm.CEMSSearch.service.CemsSearchService;
import ceu.biolab.cmm.CEMSSearch.repository.CemsSearchRepository;
import ceu.biolab.cmm.CEMSSearch.dto.CemsFeatureQueryDTO;
import ceu.biolab.cmm.shared.domain.compound.CompoundType;
import ceu.biolab.cmm.shared.domain.msFeature.*;
import jakarta.validation.Validation;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Exercises real backend validation/orchestration with deterministic repository results; no external database. */
class LegacyBackendIntegrationTest {
    private jakarta.validation.ValidatorFactory factory;
    private LegacyOptionsMapper options;
    private LegacyResponseMapper responses;
    @BeforeEach void setup() { factory=Validation.buildDefaultValidatorFactory();options=new LegacyOptionsMapper(factory.getValidator());responses=new LegacyResponseMapper(options); }
    @AfterEach void close() { factory.close(); }
    @Test void msmsAdapterAndRealServiceHonorPredictedSelection() throws Exception {
        var repo=mock(MSMSSearchRepository.class);
        when(repo.findMatchingCompoundsAndSpectra(any())).thenReturn(new MSMSSearchResponseDTO());
        var service=new LegacyMsSearchService(null,null,new MSMSSearchService(repo),options,responses);
        var input=new LegacyMsmsRequest();input.setIonMass(101d);input.setSpectraTypes(Set.of("predicted"));
        var peak=new LegacyPeak();peak.setMz(50d);peak.setIntensity(10d);input.setPeaks(List.of(peak));
        assertTrue(service.msms(input).results().isEmpty());
        var captor=ArgumentCaptor.forClass(ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchRequestDTO.class);
        verify(repo).findMatchingCompoundsAndSpectra(captor.capture());
        assertEquals(ceu.biolab.cmm.MSMSSearch.domain.SpectrumSource.PREDICTED,captor.getValue().getSpectrumSource());
    }
    @Test void ceAdapterRunsRealSearchWithConvertedConditions() throws Exception {
        var repository=mock(CemsSearchRepository.class);when(repository.findMatchingCompounds(any())).thenReturn(List.of());
        var input=new LegacyCeRequest();input.setMasses(List.of(101d));input.setEffectiveMobilities(List.of(10d));
        input.setEffMobTolerance(10d);input.setEffMobToleranceMode("percentage");input.setAdducts(List.of("M+H"));
        var request=new LegacyCeRequestMapper(options).effmob(input);
        var response=new CemsSearchService(repository).search(request);
        assertEquals(1,response.getCeFeatures().size());assertTrue(response.getCeFeatures().getFirst().getAnnotationsByAdducts().getFirst().getAnnotations().isEmpty());
        verify(repository).findMatchingCompounds(any(CemsFeatureQueryDTO.class));
    }
    @Test void realLcmsAndScoringProduceLegacyResponseWithoutDatabase() throws Exception {
        var repository=mock(CompoundRepository.class);
        var c=LegacyResponseMapperTest.compound();
        var feature=new AnnotatedFeature(101d);feature.addCompoundForAdduct("[M+H]+",c);
        when(repository.annotateMSFeature(any(),any(),any(),any(),any(),any(),any(),any(),any())).thenReturn(List.of(feature));
        var compounds=new CompoundService();ReflectionTestUtils.setField(compounds,"compoundRepository",repository);
        var ms=new LegacyMsSearchService(compounds,null,null,options,responses);
        var lcms=new BatchAdvancedSearchService();ReflectionTestUtils.setField(lcms,"compoundService",compounds);
        var advanced=new LegacyAdvancedSearchService(lcms,ms,options,responses);
        var request=new LegacyAdvancedBatchRequest();request.setMasses(List.of(101d));request.setRetentionTimes(List.of(2d));request.setAdducts(List.of("M+H"));
        var peak=new LegacyPeak();peak.setMz(101d);peak.setIntensity(10d);request.setCompositeSpectra(List.of(List.of(peak)));
        assertEquals(1,advanced.batch(request,false).results().size());
        assertInstanceOf(ILCMSFeature.class,feature.getFeature());assertEquals(2,((ILCMSFeature)feature.getFeature()).getRtValue());
    }
}
