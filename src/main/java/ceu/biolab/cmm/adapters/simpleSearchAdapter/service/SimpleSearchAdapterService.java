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

import ceu.biolab.cmm.adapters.simpleSearchAdapter.domain.LegacyDatabase;
import ceu.biolab.cmm.adapters.simpleSearchAdapter.domain.LegacyMassesMode;
import ceu.biolab.cmm.adapters.simpleSearchAdapter.domain.LegacyMetaboliteType;
import ceu.biolab.cmm.adapters.simpleSearchAdapter.dto.SimpleSearchAdapterRequestDTO;
import ceu.biolab.cmm.adapters.simpleSearchAdapter.dto.SimpleSearchAdapterResponseDTO;

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

    //We do not use rules in simple search, so we set them to a default value (-2).
    public static final int SCORE_VALUE = -2;


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

        MetaboliteType metaboliteType = correspondingMetaboliteType(request.getMetaboliteTypes());
        Set<Database> databases = correspondingDatabases(request.getDatabases());
        
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
            request.getAdducts(),                 // adductsString
            Optional.empty(),                     // detectedAdduct (ignored)
            Optional.empty(),                     // formulaType (ignored)
            databases,                            // databases
            metaboliteType                        // metaboliteType
        );
    }


    /**
     * Maps LegacyMetaboliteType to MetaboliteType. It redundantly checks for unsupported types.
     * @param legacyMetaboliteType LegacyMetaboliteType
     * @return MetaboliteType
     */
    public MetaboliteType correspondingMetaboliteType(LegacyMetaboliteType legacyMetaboliteType){
        
        if(legacyMetaboliteType == LegacyMetaboliteType.ONLY_LIPIDS){
            return MetaboliteType.ONLYLIPIDS;
        } else {
            return MetaboliteType.ALL;
        }
    }


    /**
     * Maps LegacyDatabase set to Database set. It redundantly checks for unsupported types.
     * We transform the names to match the enum names in Database. Using upperCase and replacing "-" with "".
     * @param legacyDatabases Set<LegacyDatabase>
     * @return Set<Database>
     */
    public Set<Database> correspondingDatabases(Set<LegacyDatabase> legacyDatabases){
        
        Set<Database> databases = new java.util.HashSet<>();

        try{
            for(LegacyDatabase db : legacyDatabases){

                databases.add(Database.valueOf(db.getValue().toUpperCase().replace("-", "")));
            }
        } catch(IllegalArgumentException e){
            throw new ResponseStatusException(HttpStatus.GONE, "One or more selected databases are no longer supported");
        }
        
        return databases;
    }


    /**
     *  Transforms the RTSearchResponseDTO to SimpleSearchAdapterResponseDTO.
     * @param adaptedResult RTSearchResponseDTO
     * @return SimpleSearchAdapterResponseDTO
     */
    public SimpleSearchAdapterResponseDTO transformResult(RTSearchResponseDTO adaptedResult) {

        //From the result, we get the annotated feature
        AnnotatedFeature result = adaptedResult.getMSFeatures().get(0);

        //From the AnnotatedFeature we can access the IMSFeature that contains the EM
        double EM = result.getFeature().getMzValue();


        // From that we take the anotations by adduct and we get the first adduct
        List<AnnotationsByAdduct> annotationsByAdducts = result.getAnnotationsByAdducts();

        Set<String> adducts = new HashSet<String>();

        for(AnnotationsByAdduct annotationsByAdduct : annotationsByAdducts){
            adducts.add(annotationsByAdduct.getAdduct());
        }

        //From there the list of anotations, and we take the first one
        List<Annotation> annotations = annotationsByAdducts.get(0).getAnnotations();
        Annotation annotation = annotations.get(0);
        
        //We extract the needed information from the annotation and compound
        int errorPpm =  annotation.getMassErrorPpm().intValue();
        Compound compound = annotation.getCompound();

        //We get information from the compound
        int identifier = compound.getCompoundId();
        String casId = compound.getCasId();
        String compoundName = compound.getCompoundName();
        String formula = compound.getFormula();
        double mass = compound.getMass();
        String inchiKey = compound.getInchiKey();
        List<String> pathways = getStringPathways(compound.getPathways());

        

        SimpleSearchAdapterResponseDTO response =  new SimpleSearchAdapterResponseDTO();


        response.setIdentifier(identifier); //Compound
        response.setEM(EM); //IMSFeature
        response.setName(compoundName); //Compound
        response.setFormula(formula); //Compound

        response.setAdducts(adducts);

        response.setErrorPpm(errorPpm); //Annotations
        response.setMolecularWeight(mass); //Compound
        response.setCas(casId); //Compound

        response.setPathways(pathways); //Compound
        response.setInChiKey(inchiKey); //Compound

        //Database related fields
        response.setKeggCompound(null);
        response.setKeggUri("https://www.kegg.jp/entry/");

        response.setHmdbCompound(null);
        response.setHmdbUri("https://hmdb.ca/metabolites/");    

        response.setLipidmapsCompound(null);
        response.setLipidmapsUri("https://www.lipidmaps.org/databases/lmissd/");
        
        response.setPubchemCompound(null);
        response.setPubchemUri("https://pubchem.ncbi.nlm.nih.gov/compound/");


        response.setIonizationScore(SCORE_VALUE); //Default 
        response.setFinalScore(SCORE_VALUE); //Default 

        //not supported anymore
        response.setMetlinCompound(null);
        response.setMetlinUri(null);


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
