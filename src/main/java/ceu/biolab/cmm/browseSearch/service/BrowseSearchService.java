package ceu.biolab.cmm.browseSearch.service;

import ceu.biolab.cmm.browseSearch.dto.BrowseQueryResponse;
import ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest;
import ceu.biolab.cmm.browseSearch.dto.BrowseSearchResponse;
import ceu.biolab.cmm.browseSearch.repository.BrowseSearchRepository;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class BrowseSearchService {

    @Autowired
    private BrowseSearchRepository browseSearchRepository;

    public BrowseQueryResponse search (BrowseSearchRequest request) {
        // Validación básica
        if ((request.getCompoundName() == null || request.getCompoundName().isBlank()) &&
                (request.getCompoundFormula() == null || request.getCompoundFormula().isBlank()) &&
                (request.getDatabases() == null ) &&
                (request.getMetaboliteType() == null )) {
            return new BrowseQueryResponse();
        }

        try {


            return browseSearchRepository.findMatchingCompounds(request);
        } catch (IOException e) {
            e.printStackTrace();
            return new BrowseQueryResponse();
        }

    }
}
