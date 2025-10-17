package ceu.biolab.cmm.browseSearch.service;

import ceu.biolab.cmm.browseSearch.dto.BrowseQueryResponse;
import ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest;
import ceu.biolab.cmm.browseSearch.repository.BrowseSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Service
public class BrowseSearchService {
    @Autowired
    private BrowseSearchRepository browseSearchRepository;

    public BrowseSearchService(BrowseSearchRepository browseSearchRepository) {
        this.browseSearchRepository = browseSearchRepository;
    }

    public BrowseQueryResponse search (BrowseSearchRequest request) {
        if (!hasValidName(request) && !hasValidFormula(request)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide a compound name (min 3 characters) or a formula.");
        }

        // Validación: bases de datos no pueden estar vacías
        if (request.getDatabases() == null || request.getDatabases().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must provide at least one database.");
        }

        // Validación: metaboliteType obligatorio
        if (request.getMetaboliteType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must provide a metabolite type.");
        }

        // Ejecución principal
        try {
            return browseSearchRepository.findMatchingCompounds(request);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing the request.", e);
        }
    }

    private boolean hasValidName(BrowseSearchRequest request) {
        String name = request.getCompoundName();
        return name != null && name.trim().length() >= 3;
    }

    private boolean hasValidFormula(BrowseSearchRequest request) {
        String formula = request.getFormula();
        return formula != null && !formula.isBlank();
    }

}
