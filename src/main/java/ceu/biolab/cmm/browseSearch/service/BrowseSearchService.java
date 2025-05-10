package ceu.biolab.cmm.browseSearch.service;

import ceu.biolab.cmm.browseSearch.dto.BrowseQueryResponse;
import ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest;
import ceu.biolab.cmm.browseSearch.repository.BrowseSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class BrowseSearchService {

    @Autowired
    private BrowseSearchRepository browseSearchRepository;

    public BrowseSearchService(BrowseSearchRepository browseSearchRepository) {
        this.browseSearchRepository = browseSearchRepository;
    }

    public BrowseQueryResponse search (BrowseSearchRequest request) {

        if ((request.getCompound_name() == null || request.getCompound_name().isBlank()) &&
                (request.getFormula() == null || request.getFormula().isBlank()) &&
                (request.getDatabases() == null ) &&
                (request.getMetaboliteType() == null )) {
            return new BrowseQueryResponse();
            // TODO como hacer para q aqui no se rompa el codigo y spolo de un mensaje
        }
        try {

            return browseSearchRepository.findMatchingCompounds(request);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.printf("error en el intento de findcompournds" );
            //TODO si crashease aqui saldria el error

            return new BrowseQueryResponse();
        }

    }
}
