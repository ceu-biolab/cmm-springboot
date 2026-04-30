package ceu.biolab.cmm.unit.MSMSSearch.service;

import ceu.biolab.cmm.MSMSSearch.domain.CIDEnergy;
import ceu.biolab.cmm.MSMSSearch.domain.SpectrumSource;
import ceu.biolab.cmm.MSMSSearch.dto.LCMSMSSearchRequestDTO;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        req.setRtValue(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.searchWithLcmsScoring(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}
