package ceu.biolab.cmm.MSMSSearch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchResponseDTO;
import ceu.biolab.cmm.MSMSSearch.repository.MSMSSearchRepository;

@Service
public class MSMSSearchService {
    private final MSMSSearchRepository msmsSearchRepository;

    @Autowired
    public MSMSSearchService(MSMSSearchRepository msmsSearchRepository) {
        this.msmsSearchRepository = msmsSearchRepository;
    }

    public MSMSSearchResponseDTO search(MSMSSearchRequestDTO request) {
        // Validaciones básicas
        if (request.getPrecursorIonMZ() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Precursor m/z is required.");
        }

        if (request.getAdducts() == null || request.getAdducts().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must provide at least one adduct.");
        }

        if (request.getIonizationMode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ionization mode is required.");
        }

        if (request.getCIDEnergy() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CID energy is required.");
        }
        if (request.getScoreType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Score type is required.");
        }
        if (request.getTolerancePrecursorIon() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Precursor tolerance must be greater than zero.");
        }
        if (request.getToleranceFragments() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fragment tolerance must be greater than zero.");
        }
        if (request.getToleranceModePrecursorIon() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Precursor tolerance mode is required.");
        }
        if (request.getToleranceModeFragments() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fragment tolerance mode is required.");
        }

        if (request.getFragmentsMZsIntensities() == null
                || request.getFragmentsMZsIntensities().getPeaks() == null
                || request.getFragmentsMZsIntensities().getPeaks().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fragment peaks are required.");
        }
        if (request.getFragmentsMZsIntensities().getPrecursorMz() == null
                || request.getFragmentsMZsIntensities().getPrecursorMz() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fragment precursor m/z must be greater than zero.");
        }

        // Execute search
        try {
            return msmsSearchRepository.findMatchingCompoundsAndSpectra(request);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to execute MS/MS search",
                    e
            );
        }
    }
}
