package ceu.biolab.cmm.adapters.browseSearchAdapter.service;
import ceu.biolab.cmm.adapters.shared.translators.MetaboliteTypeTranslation;

import org.springframework.stereotype.Service;

import ceu.biolab.cmm.adapters.browseSearchAdapter.dto.BrowseSearchAdapterRequestDTO;
import ceu.biolab.cmm.adapters.browseSearchAdapter.dto.BrowseSearchAdapterResponseDTO;

/**
 * Browse Search Adapter Service.
 *
 * Provides search functionality for browse-style queries.
 * This is currently a placeholder; implementation will be added later.
 */

@Service
public class BrowseSearchAdapterService {

    /**
     * Main entry point for the browse search adapter.
     * For now, this method only returns an empty response.
     *
     * @param request BrowseSearchAdapterRequestDTO containing search parameters
     * @return BrowseSearchAdapterResponseDTO with search results
     */
    @org.springframework.beans.factory.annotation.Autowired
    private ceu.biolab.cmm.browseSearch.service.BrowseSearchService browseSearchService;


    public BrowseSearchAdapterResponseDTO search(BrowseSearchAdapterRequestDTO request) {
        // 1. Validate and translate from adapter/legacy to internal
        ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest internalRequest = adaptRequest(request);
        // 2. Call the new internal service
        ceu.biolab.cmm.browseSearch.dto.BrowseQueryResponse internalResponse = browseSearchService.search(internalRequest);
        // 3. Adapt the internal response to the adapter DTO
        return adaptResponse(internalResponse);
    }

    private ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest adaptRequest(BrowseSearchAdapterRequestDTO request) {

        java.util.Set<ceu.biolab.cmm.shared.domain.Database> dbSet = null;
        if (request.getDatabases() != null) {
            dbSet = ceu.biolab.cmm.adapters.shared.translators.DatabaseTranslation.toDatabases(new java.util.HashSet<>(request.getDatabases()));
        }

        ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest internalRequest = new ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest();
        internalRequest.setCompoundName(request.getName());
        internalRequest.setFormula(request.getFormula());
        if (dbSet != null) {
            internalRequest.setDatabases(dbSet);
        }
        
        internalRequest.setMetaboliteType(MetaboliteTypeTranslation.toMetaboliteType(request.getMetabolitesType()));
        internalRequest.setExactName(request.isExactName());
        return internalRequest;
    }

    private BrowseSearchAdapterResponseDTO adaptResponse(ceu.biolab.cmm.browseSearch.dto.BrowseQueryResponse internalResponse) {
        java.util.List<BrowseSearchAdapterResponseDTO.TheoreticalCompound> theoreticalCompounds = new java.util.ArrayList<>();
        if (internalResponse != null && internalResponse.getCompoundlist() != null) {
                for (ceu.biolab.cmm.shared.domain.compound.Compound compound : internalResponse.getCompoundlist()) {

                java.util.List<ceu.biolab.cmm.shared.domain.compound.Pathway> pathwayList =
                    new java.util.ArrayList<>(compound.getPathways());

                BrowseSearchAdapterResponseDTO.TheoreticalCompound.TheoreticalCompoundBuilder builder = BrowseSearchAdapterResponseDTO.TheoreticalCompound.builder()
                    .identifier(compound.getCompoundId())
                    .name(compound.getCompoundName())
                    .formula(compound.getFormula())
                    .molecularWeight(compound.getMass())
                    .cas(compound.getCasId())
                    .inChiKey(compound.getInchiKey())
                    .pathways(pathwayList);

                // If CMMCompound, extract database IDs
                if (compound instanceof ceu.biolab.cmm.shared.domain.compound.CMMCompound) {
                    ceu.biolab.cmm.shared.domain.compound.CMMCompound cmm = (ceu.biolab.cmm.shared.domain.compound.CMMCompound) compound;
                    builder.hmdbCompound(cmm.getHmdbID() != null ? cmm.getHmdbID() : "");
                    builder.hmdbUri(cmm.getHmdbID() != null ? "https://hmdb.ca/metabolites/" + cmm.getHmdbID() : "");
                    builder.keggCompound(cmm.getKeggID() != null ? cmm.getKeggID() : "");
                    builder.keggUri(cmm.getKeggID() != null ? "https://www.kegg.jp/entry/" + cmm.getKeggID() : "");
                    builder.lipidmapsCompound(cmm.getLmID() != null ? cmm.getLmID() : "");
                    builder.lipidmapsUri(cmm.getLmID() != null ? "https://www.lipidmaps.org/databases/lmissd/" + cmm.getLmID() : "");
                    builder.pubchemCompound(cmm.getPcID() != null ? cmm.getPcID().toString() : "");
                    builder.pubchemUri(cmm.getPcID() != null ? "https://pubchem.ncbi.nlm.nih.gov/compound/" + cmm.getPcID() : "");
                } else {
                    builder.hmdbCompound("");
                    builder.hmdbUri("");
                    builder.keggCompound("");
                    builder.keggUri("");
                    builder.lipidmapsCompound("");
                    builder.lipidmapsUri("");
                    builder.pubchemCompound("");
                    builder.pubchemUri("");
                }
                // Metlin not supported
                builder.metlinCompound("");
                builder.metlinUri("");

                theoreticalCompounds.add(builder.build());
            }
        }
        return BrowseSearchAdapterResponseDTO.builder()
                .theoreticalCompounds(theoreticalCompounds)
                .build();
    }
}
