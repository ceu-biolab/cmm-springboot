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
        if (request.getPrecursorIonMZ() == 0) {
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

        // Execute search
        try {
            return msmsSearchRepository.findMatchingCompoundsAndSpectra(request);
        } catch (Exception e) {
            return new MSMSSearchResponseDTO();
        }
    }
}
