package ceu.biolab.cmm.adapters.simpleSearchAdapter.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.compound.Pathway;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import ceu.biolab.cmm.shared.domain.msFeature.Annotation;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotationsByAdduct;
import ceu.biolab.cmm.shared.domain.Constants;

import ceu.biolab.cmm.adapters.shared.domain.LegacyDatabase;
import ceu.biolab.cmm.adapters.shared.domain.LegacyMassesMode;
import ceu.biolab.cmm.adapters.shared.domain.LegacyMetaboliteType;
import ceu.biolab.cmm.adapters.simpleSearchAdapter.dto.SimpleSearchAdapterRequestDTO;
import ceu.biolab.cmm.adapters.simpleSearchAdapter.dto.SimpleSearchAdapterResponseDTO;
import ceu.biolab.cmm.adapters.shared.translators.AdductTranslation;
import ceu.biolab.cmm.adapters.shared.translators.MetaboliteTypeTranslation;
import ceu.biolab.cmm.adapters.shared.translators.DatabaseTranslation;

import ceu.biolab.cmm.msSearch.dto.CompoundSimpleSearchRequestDTO;
import ceu.biolab.cmm.msSearch.dto.RTSearchResponseDTO;
import ceu.biolab.cmm.msSearch.controller.CompoundController;



/*
 *  Simple Search Adapter Service
 *   
 *  Provides search functionality for single-compound queries. Adapts requests to the new internal search engine. (V2/V3 - V4)
 *  
 *  Uses:
 *   - SimpleSearchAdapterRequestDTO for input
 *   - CompoundSimpleSearchRequestDTO for internal search
 *   - RTSearchResponseDTO for internal search results
 *   - SimpleSearchAdapterResponseDTO for output
 */

@Service
public class SimpleSearchAdapterService {

    //We do not use rules in simple search, so we set them to a default value (-2.0).
    public static final double SCORE_VALUE = -2.0;


    private final CompoundController existingEndpoint;
    
    public SimpleSearchAdapterService(CompoundController existingEndpoint) {
        this.existingEndpoint = existingEndpoint;
    }

    


    /**
     * Performs a search based on the provided request DTO. It checks the validity of the request, adapts it to the internal format,
     * calls the CompoundService to perform the search, and then adapts the results back to the response DTO format.
     *
     * @param request SimpleSearchAdapterRequestDTO containing search parameters
     * @return SimpleSearchAdapterResponseDTO with search results
     */

    public SimpleSearchAdapterResponseDTO search(SimpleSearchAdapterRequestDTO request) {

        validateRequest(request);

        CompoundSimpleSearchRequestDTO adaptedRequest = transformRequest(request);
        RTSearchResponseDTO adaptedResult = existingEndpoint.annotateMSFeature(adaptedRequest);

        
        SimpleSearchAdapterResponseDTO response = transformResult(adaptedResult);

        return response;
    }



    /**
     * Validates the request DTO fields. Throws exceptions for invalid data.
     * 
     * We specifically check for:
     * - Mass must be positive
     * - Tolerance must be in the range (0,100)
     * - Ionization mode NEUTRAL is not implemented
     * - Certain databases are no longer supported
     * - Metabolite type ALL_INCLUDING_PEPTIDES, as peptide search is no longer supported
     *
     * @param request SimpleSearchAdapterRequestDTO to validate
     */
    public void validateRequest(SimpleSearchAdapterRequestDTO request) {

        if (request.getMass() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mass must be a positive number.");
        }

        if (request.getTolerance() < 0 || request.getTolerance() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tolerance must be in the range (0,100).");
        }

        if(request.getIonMode() == IonizationMode.NEUTRAL){
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Neutral mode is not implemented yet");
        }

        //These databases are no longer supported, becouse of that, the option to select all, must also thrown an error
        for(LegacyDatabase db : request.getDatabases()){
            if(db == LegacyDatabase.MINE || db == LegacyDatabase.METLIN){
                throw new ResponseStatusException(HttpStatus.GONE, "Database "+ db.getValue() + " is no longer supported");
            }
        }
    }



    /**
     * Transforms the SimpleSearchAdapterRequestDTO to CompoundSimpleSearchRequestDTO.
     * To do that we first map the old domains to the shared and already used ones. We do the following transformations:
     * - LegacyMetaboliteType to MetaboliteType
     * - LegacyDatabase set to Database set
     * - Neutral mass to mz
     * 
     * @param request SimpleSearchAdapterRequestDTO
     * @return CompoundSimpleSearchRequestDTO
     */
    public CompoundSimpleSearchRequestDTO transformRequest(SimpleSearchAdapterRequestDTO request) {

        MetaboliteType metaboliteType = MetaboliteTypeTranslation.toMetaboliteType(request.getMetaboliteTypes());
        Set<Database> databases = DatabaseTranslation.toDatabases(request.getDatabases());
        
        // Transform adducts to canonical [..]+ / [..]- format
        List<String> transformedAdducts = AdductTranslation.translateAll(request.getAdducts(), request.getIonMode());

        double mass = request.getMass();

        if(request.getMassesMode() == LegacyMassesMode.NEUTRAL){

            switch(request.getIonMode()){
                case POSITIVE:
                    mass += Constants.PROTON_WEIGTH;
                    break;
                case NEGATIVE:
                    mass -= Constants.PROTON_WEIGTH;
                    break;
                case NEUTRAL:
                    break;
            }
        }

        return new CompoundSimpleSearchRequestDTO(
            mass,                                 // mz
            request.getToleranceMode(),           // mzToleranceMode
            request.getTolerance(),               // tolerance
            request.getIonMode(),                 // ionizationMode
            new HashSet<>(transformedAdducts),    // adductsString
            Optional.empty(),                     // detectedAdduct (ignored)
            Optional.empty(),                     // formulaType (ignored)
            databases,                            // databases
            metaboliteType                        // metaboliteType
        );
    }


    /**
     *  Transforms the RTSearchResponseDTO to SimpleSearchAdapterResponseDTO.
     * @param adaptedResult RTSearchResponseDTO
     * @return SimpleSearchAdapterResponseDTO
     */
    public SimpleSearchAdapterResponseDTO transformResult(RTSearchResponseDTO adaptedResult) {

        SimpleSearchAdapterResponseDTO response = new SimpleSearchAdapterResponseDTO();
        List<SimpleSearchAdapterResponseDTO.Result> results = new ArrayList<>();
        response.setResults(results);

        if (adaptedResult == null || adaptedResult.getMSFeatures() == null || adaptedResult.getMSFeatures().isEmpty()) {
            return response;
        }

        for (AnnotatedFeature feature : adaptedResult.getMSFeatures()) {
            if (feature == null || feature.getFeature() == null) {
                continue;
            }
            double em = feature.getFeature().getMzValue();
            List<AnnotationsByAdduct> annotationsByAdducts = feature.getAnnotationsByAdducts();
            if (annotationsByAdducts != null && !annotationsByAdducts.isEmpty()) {
                for (AnnotationsByAdduct group : annotationsByAdducts) {
                    if (group == null || group.getAnnotations() == null) {
                        continue;
                    }
                    List<Annotation> annotations = group.getAnnotations();
                    if (annotations.isEmpty()) {
                        continue;
                    }
                    // Only the adduct(s) for this group
                    String groupAdducts = "";
                    if (group.getAdduct() != null) {
                        groupAdducts = AdductTranslation.reverse(group.getAdduct());
                    }
                    for (Annotation annotation : annotations) {
                        if (annotation == null) {
                            continue;
                        }
                        SimpleSearchAdapterResponseDTO.Result dto = new SimpleSearchAdapterResponseDTO.Result();
                        dto.setEM(em);
                        dto.setAdduct(groupAdducts);
                        dto.setIonizationScore(SCORE_VALUE); // Default
                        dto.setFinalScore(SCORE_VALUE);      // Default
                        if (annotation.getMassErrorPpm() != null) {
                            //The values are sign inverted in the new search, so we need to multiply by -1 to keep the same behavior in the adapter
                            dto.setErrorPpm((-1) * annotation.getMassErrorPpm().intValue());
                        }
                        Compound compound = annotation.getCompound();
                        if (compound != null) {
                            dto.setIdentifier(compound.getCompoundId());
                            dto.setCas(compound.getCasId());
                            dto.setName(compound.getCompoundName());
                            dto.setFormula(compound.getFormula());
                            dto.setMolecularWeight(compound.getMass());
                            dto.setInChiKey(compound.getInchiKey());
                            dto.setPathways(getStringPathways(compound.getPathways()));
                            // Database info -> CMMCompound
                            if (compound instanceof ceu.biolab.cmm.shared.domain.compound.CMMCompound cmm) {
                                dto.setKeggCompound(cmm.getKeggID() != null ? cmm.getKeggID() : "");
                                dto.setKeggUri(cmm.getKeggID() != null ? "https://www.kegg.jp/entry/" + cmm.getKeggID() : "");
                                dto.setHmdbCompound(cmm.getHmdbID() != null ? cmm.getHmdbID() : "");
                                dto.setHmdbUri(cmm.getHmdbID() != null ? "https://hmdb.ca/metabolites/" + cmm.getHmdbID() : "");
                                dto.setLipidmapsCompound(cmm.getLmID() != null ? cmm.getLmID() : "");
                                dto.setLipidmapsUri(cmm.getLmID() != null ? "https://www.lipidmaps.org/databases/lmissd/" + cmm.getLmID() : "");
                                dto.setPubchemCompound(cmm.getPcID() != null ? cmm.getPcID().toString() : "");
                                dto.setPubchemUri(cmm.getPcID() != null ? "https://pubchem.ncbi.nlm.nih.gov/compound/" + cmm.getPcID() : "");
                            } else {
                                dto.setKeggCompound("");
                                dto.setKeggUri("");
                                dto.setHmdbCompound("");
                                dto.setHmdbUri("");
                                dto.setLipidmapsCompound("");
                                dto.setLipidmapsUri("");
                                dto.setPubchemCompound("");
                                dto.setPubchemUri("");
                            }
                        }
                        // not supported anymore
                        dto.setMetlinCompound("");
                        dto.setMetlinUri("");
                        results.add(dto);
                    }
                }
            }
        }
        return response;
    }


    private List<String> getStringPathways(Set<Pathway> pathways){

        List<String> pathwayList = new ArrayList<>();

        for(Pathway path :pathways){
            pathwayList.add(path.toString());
        }

        return pathwayList;
    }
}
