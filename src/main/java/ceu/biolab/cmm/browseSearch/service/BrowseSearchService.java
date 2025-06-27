package ceu.biolab.cmm.browseSearch.service;

import ceu.biolab.cmm.browseSearch.dto.BrowseQueryResponse;
import ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest;
import ceu.biolab.cmm.browseSearch.repository.BrowseSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Arrays;

@Service
public class BrowseSearchService {

    @Autowired
    private BrowseSearchRepository browseSearchRepository;

    public BrowseSearchService(BrowseSearchRepository browseSearchRepository) {
        this.browseSearchRepository = browseSearchRepository;
    }

    public BrowseQueryResponse search(BrowseSearchRequest request) {

        // Validación: al menos compound_name o formula debe estar presente
        if ((request.getCompound_name() == null || request.getCompound_name().isBlank()) &&
                (request.getFormula() == null || request.getFormula().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must provide at least a compound name or a formula.");
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
            System.out.println("Searching for: " + request.getCompound_name()+" with formula: " + request.getFormula()+" in databases: " + Arrays.toString(request.getDatabases().toArray()) +" and metabolite type: " + request.getMetaboliteType().name()+" with exact name: " + request.isExact_name());
            return browseSearchRepository.findMatchingCompounds(request);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error during compound search.");
            return new BrowseQueryResponse(); // vacío si hay error
        }
    }

}
