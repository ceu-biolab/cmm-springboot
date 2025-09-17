package ceu.biolab.cmm.browseSearch.service;

import ceu.biolab.cmm.browseSearch.dto.BrowseQueryResponse;
import ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest;
import ceu.biolab.cmm.browseSearch.repository.BrowseSearchRepository;
import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class BrowseSearchService {
    @Autowired
    private BrowseSearchRepository browseSearchRepository;

    public BrowseSearchService(BrowseSearchRepository browseSearchRepository) {
        this.browseSearchRepository = browseSearchRepository;
    }

    public BrowseQueryResponse search (BrowseSearchRequest request) {
        String compoundName = request.getCompoundName();
        String formula = request.getFormula();

        if (!hasValidName(request) || !hasValidFormula(request)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide a compound name (min 3 characters) or a formula.");
        }

        // Validación: bases de datos no pueden estar vacías
        if (request.getDatabases() == null || request.getDatabases().isEmpty()) {
            //** Alternative: set a default value
            Set<Database> databases = new HashSet<>();
            databases.add(Database.ALL);
            request.setDatabases(databases);
            //throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must provide at least one database.");
        }

        // Validación: metaboliteType obligatorio
        if (request.getMetaboliteType() == null) {
            //throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must provide a metabolite type.");
            //** Alternative: set a default value
            request.setMetaboliteType(MetaboliteType.ALL);
        }

        // Ejecución principal
        try {
            System.out.println("Searching for: " + request.getCompoundName() +
                    " with formula: " + request.getFormula() +
                    " in databases: " + Arrays.toString(request.getDatabases().toArray()) +
                    " and metabolite type: " + request.getMetaboliteType().name() +
                    " with exact name: " + request.isExactName());
            return browseSearchRepository.findMatchingCompounds(request);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error during compound search.");
            return new BrowseQueryResponse(); // vacío si hay error
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
