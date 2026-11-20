package ceu.biolab.cmm.unit.MSMSSearch.service;

import ceu.biolab.cmm.MSMSSearch.domain.CIDEnergy;
import ceu.biolab.cmm.MSMSSearch.domain.MSMSAnnotation;
import ceu.biolab.cmm.MSMSSearch.domain.SpectrumSource;
import ceu.biolab.cmm.MSMSSearch.dto.LCMSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMSSearch.dto.LCMSMSSearchResponseDTO;
import ceu.biolab.cmm.shared.domain.msFeature.ScoreType;
import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchResponseDTO;
import ceu.biolab.cmm.MSMSSearch.repository.MSMSSearchRepository;
import ceu.biolab.cmm.MSMSSearch.service.MSMSSearchService;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.MSMSSearch.domain.Spectrum;
import ceu.biolab.cmm.shared.domain.msFeature.MSPeak;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MSMSSearchServiceTest {

    private MSMSSearchRepository repository;
    private MSMSSearchService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(MSMSSearchRepository.class);
        service = new MSMSSearchService(repository);
    }

    private MSMSSearchRequestDTO validRequest() {
        Spectrum spectrum = new Spectrum(500.0, List.of(new MSPeak(100.0, 10.0)));
        return new MSMSSearchRequestDTO(
                CIDEnergy.MED,
                500.0,
                10.0,
                MzToleranceMode.PPM,
                50.0,
                MzToleranceMode.MDA,
                IonizationMode.POSITIVE,
                List.of("[M+H]+"),
                spectrum,
                ScoreType.COSINE
        );
    }

    @Test
    void search_throwsWhenPrecursorMzMissing() {
        MSMSSearchRequestDTO req = validRequest();
        req.setPrecursorIonMZ(0.0);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.search(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void search_throwsWhenNoAdducts() {
        MSMSSearchRequestDTO req = validRequest();
        req.setAdducts(List.of());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.search(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void search_throwsWhenIonModeNull() {
        MSMSSearchRequestDTO req = validRequest();
        req.setIonizationMode(null);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.search(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void search_throwsWhenIonModeNeutral() {
        MSMSSearchRequestDTO req = validRequest();
        req.setIonizationMode(IonizationMode.NEUTRAL);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.search(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void search_throwsWhenCIDEnergyNull() {
        MSMSSearchRequestDTO req = validRequest();
        req.setCIDEnergy(null);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.search(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void search_throwsWhenSpectrumSourceNull() {
        MSMSSearchRequestDTO req = validRequest();
        req.setSpectrumSource(null);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.search(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void search_acceptsMissingFragmentPrecursorMz() throws Exception {
        MSMSSearchResponseDTO expected = new MSMSSearchResponseDTO();
        when(repository.findMatchingCompoundsAndSpectra(any())).thenReturn(expected);

        MSMSSearchRequestDTO req = validRequest();
        req.getFragmentsMZsIntensities().setPrecursorMz(null);

        MSMSSearchResponseDTO resp = service.search(req);
        assertSame(expected, resp);
    }

    @Test
    void search_returnsRepositoryResponse() throws Exception {
        MSMSSearchResponseDTO expected = new MSMSSearchResponseDTO();
        when(repository.findMatchingCompoundsAndSpectra(any())).thenReturn(expected);

        MSMSSearchResponseDTO resp = service.search(validRequest());
        assertSame(expected, resp);
    }

    @Test
    void search_throwsWhenPrecursorToleranceExceedsMaxForPpm() {
        MSMSSearchRequestDTO req = validRequest();
        req.setToleranceModePrecursorIon(MzToleranceMode.PPM);
        req.setTolerancePrecursorIon(101.0);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.search(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void search_throwsWhenFragmentToleranceExceedsMaxForMda() {
        MSMSSearchRequestDTO req = validRequest();
        req.setToleranceModeFragments(MzToleranceMode.MDA);
        req.setToleranceFragments(101.0);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.search(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void searchWithLcmsScoring_throwsWhenRtMissing() {
        LCMSMSSearchRequestDTO req = new LCMSMSSearchRequestDTO();
        req.setCIDEnergy(CIDEnergy.MED);
        req.setTolerancePrecursorIon(10.0);
        req.setToleranceModePrecursorIon(MzToleranceMode.PPM);
        req.setToleranceFragments(50.0);
        req.setToleranceModeFragments(MzToleranceMode.MDA);
        req.setIonizationMode(IonizationMode.POSITIVE);
        req.setAdducts(List.of("[M+H]+"));
        req.setFragmentsMZsIntensities(new Spectrum(500.0, List.of(new MSPeak(100.0, 10.0))));
        req.setPrecursorIonMZ(500.0);
        req.setScoreType(ScoreType.COSINE);
        req.setSpectrumSource(SpectrumSource.ALL);
        req.setRtValue(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.searchWithLcmsScoring(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void searchWithLcmsScoring_throwsWhenBatchLengthsDiffer() {
        LCMSMSSearchRequestDTO req = validLcmsmsRequest();
        req.setPrecursorIonMZ(null);
        req.setFragmentsMZsIntensities(null);
        req.setRtValue(null);
        req.setPrecursorIonMZValues(List.of(500.0, 600.0));
        req.setFragmentsMZsIntensitiesList(List.of(
                new Spectrum(500.0, List.of(new MSPeak(100.0, 10.0))),
                new Spectrum(600.0, List.of(new MSPeak(120.0, 20.0)))
        ));
        req.setRtValues(List.of(5.1));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.searchWithLcmsScoring(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void searchWithLcmsScoring_supportsBatchedFeatures() throws Exception {
        when(repository.findMatchingCompoundsAndSpectra(any())).thenAnswer(invocation -> {
            MSMSSearchRequestDTO request = invocation.getArgument(0);
            MSMSSearchResponseDTO response = new MSMSSearchResponseDTO(new ArrayList<MSMSAnnotation>());
            response.setExperimentalSpectrum(request.getFragmentsMZsIntensities());
            return response;
        });

        LCMSMSSearchRequestDTO req = validLcmsmsRequest();
        req.setPrecursorIonMZ(null);
        req.setFragmentsMZsIntensities(null);
        req.setRtValue(null);
        req.setPrecursorIonMZValues(List.of(500.0, 600.0));
        req.setFragmentsMZsIntensitiesList(List.of(
                new Spectrum(500.0, List.of(new MSPeak(100.0, 10.0))),
                new Spectrum(600.0, List.of(new MSPeak(120.0, 20.0)))
        ));
        req.setRtValues(List.of(5.1, 6.2));

        LCMSMSSearchResponseDTO response = service.searchWithLcmsScoring(req);

        assertEquals(2, response.getMsmsFeatures().size());
        assertNull(response.getMsmsList());
        assertNull(response.getExperimentalSpectrum());
        assertEquals(500.0, response.getMsmsFeatures().get(0).getFeature().getMzValue());
        assertEquals(5.1, response.getMsmsFeatures().get(0).getFeature().getRtValue());
        assertEquals(600.0, response.getMsmsFeatures().get(1).getFeature().getMzValue());
        assertEquals(6.2, response.getMsmsFeatures().get(1).getFeature().getRtValue());
        verify(repository, times(2)).findMatchingCompoundsAndSpectra(any());
    }

    private LCMSMSSearchRequestDTO validLcmsmsRequest() {
        LCMSMSSearchRequestDTO req = new LCMSMSSearchRequestDTO();
        req.setCIDEnergy(CIDEnergy.MED);
        req.setPrecursorIonMZ(500.0);
        req.setTolerancePrecursorIon(10.0);
        req.setToleranceModePrecursorIon(MzToleranceMode.PPM);
        req.setToleranceFragments(50.0);
        req.setToleranceModeFragments(MzToleranceMode.MDA);
        req.setIonizationMode(IonizationMode.POSITIVE);
        req.setAdducts(List.of("[M+H]+"));
        req.setFragmentsMZsIntensities(new Spectrum(500.0, List.of(new MSPeak(100.0, 10.0))));
        req.setScoreType(ScoreType.COSINE);
        req.setSpectrumSource(SpectrumSource.ALL);
        req.setRtValue(5.0);
        return req;
    }

    @Test
    void batchFeaturesShareRetentionTimeEvidenceAndKeepRequestOrder() throws Exception {
        when(repository.findMatchingCompoundsAndSpectra(any())).thenAnswer(invocation -> {
            var input = (MSMSSearchRequestDTO) invocation.getArgument(0);
            boolean first = input.getPrecursorIonMZ() == 800.0;
            var hit = new MSMSAnnotation();
            hit.setAdduct("[M+H]+");
            hit.setCompound(ceu.biolab.cmm.shared.domain.compound.Compound.builder()
                    .compoundId(first ? 1 : 2)
                    .compoundType(ceu.biolab.cmm.shared.domain.compound.CompoundType.LIPID)
                    .lipidType("PC").numChains(2).numCarbons(36).doubleBonds(first ? 2 : 4).build());
            return new MSMSSearchResponseDTO(new ArrayList<>(List.of(hit)));
        });
        var request = validLcmsmsRequest();
        request.setPrecursorIonMZ(null);
        request.setRtValue(null);
        request.setFragmentsMZsIntensities(null);
        request.setPrecursorIonMZValues(List.of(800.0, 802.0));
        request.setRtValues(List.of(6.0, 5.0));
        request.setFragmentsMZsIntensitiesList(List.of(new Spectrum(800.0, List.of(new MSPeak(100, 1))),
                new Spectrum(802.0, List.of(new MSPeak(100, 1)))));

        var response = service.searchWithLcmsScoring(request);

        assertEquals(800.0, response.getMsmsFeatures().getFirst().getFeature().getMzValue());
        assertEquals(802.0, response.getMsmsFeatures().getLast().getFeature().getMzValue());
        for (var feature : response.getMsmsFeatures()) {
            var scores = (ceu.biolab.cmm.scoreAnnotations.domain.CompoundScores)
                    feature.getMsmsList().getFirst().getScores().getFirst();
            assertEquals(java.util.Optional.of(1.0), scores.getRtScore());
        }
    }
}
