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

    public BrowseSearchResponse search (BrowseSearchRequest request) {
        // Validación básica
        if ((request.getCompoundName() == null || request.getCompoundName().isBlank()) &&
                (request.getCompoundFormula() == null || request.getCompoundFormula().isBlank()) &&
                (request.getDatabases() == null ) &&
                (request.getMetaboliteType() == null )) {
            return new BrowseSearchResponse();
        }

        try {
            List<BrowseQueryResponse> queryResults = browseSearchRepository.findMatchingCompounds(request);
            BrowseSearchResponse searchResults = new BrowseSearchResponse();
            for (BrowseQueryResponse queryResult : queryResults) {
                searchResults.addCompound(new Compound(queryResult.getCompoundId(),queryResult.getCasId(), queryResult.getCompoundName(),queryResult.getFormula(), queryResult.getMass(),
                        queryResult.getChargeType(),queryResult.getChargeNumber(),queryResult.getFormulaType(),queryResult.getCompoundType(),queryResult.getCompoundStatus(),queryResult.getFormulaTypeInt(),
                        queryResult.getLogP(),queryResult.getRtPred(),queryResult.getInchi(),queryResult.getInchiKey(),queryResult.getSmiles(),queryResult.getLipidType(),queryResult.getNumChains(),queryResult.getNumCarbons(),
                        queryResult.getDoubleBonds(),queryResult.getBiologicalActivity(),queryResult.getMeshNomenclature(),queryResult.getIupacClassification()));
            }

            return  searchResults;
        } catch (IOException e) {
            e.printStackTrace();
            return new BrowseSearchResponse();
        }

    }
}
