package ceu.biolab.cmm.MSMS.service;

import ceu.biolab.cmm.MSMS.dto.MSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMS.dto.MSMSSearchResponseDTO;
import ceu.biolab.cmm.MSMS.repository.MSMSSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

        // Ejecución principal
        try {
            System.out.println("Starting MSMS search with precursor m/z: " + request.getPrecursorIonMZ());
            return msmsSearchRepository.findMatchingCompoundsAndSpectra(request);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error during MSMS search.");
            return new MSMSSearchResponseDTO(); // devolver vacío si falla
        }
    }
}
