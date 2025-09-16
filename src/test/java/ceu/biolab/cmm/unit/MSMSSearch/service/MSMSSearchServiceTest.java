package ceu.biolab.cmm.unit.MSMSSearch.service;

import ceu.biolab.cmm.MSMSSearch.domain.CIDEnergy;
import ceu.biolab.cmm.MSMSSearch.domain.ScoreType;
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
                List.of("M+H"),
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
    void search_throwsWhenCIDEnergyNull() {
        MSMSSearchRequestDTO req = validRequest();
        req.setCIDEnergy(null);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.search(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void search_returnsRepositoryResponse() throws Exception {
        MSMSSearchResponseDTO expected = new MSMSSearchResponseDTO();
        when(repository.findMatchingCompoundsAndSpectra(any())).thenReturn(expected);

        MSMSSearchResponseDTO resp = service.search(validRequest());
        assertSame(expected, resp);
    }

    @Test
    void search_returnsEmptyOnRepositoryException() throws Exception {
        when(repository.findMatchingCompoundsAndSpectra(any())).thenThrow(new RuntimeException("boom"));
        MSMSSearchResponseDTO resp = service.search(validRequest());
        assertNotNull(resp);
    }
}

