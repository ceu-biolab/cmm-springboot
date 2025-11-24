package ceu.biolab.cmm.adapters.simpleSearchAdapter.service;

import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import ceu.biolab.cmm.adapters.simpleSearchAdapter.domain.LegacyDatabase;
import ceu.biolab.cmm.adapters.simpleSearchAdapter.domain.LegacyMassesMode;
import ceu.biolab.cmm.adapters.simpleSearchAdapter.domain.LegacyMetaboliteType;

import ceu.biolab.cmm.adapters.simpleSearchAdapter.dto.SimpleSearchAdapterRequestDTO;
import ceu.biolab.cmm.adapters.simpleSearchAdapter.dto.SimpleSearchAdapterResponseDTO;

import ceu.biolab.cmm.msSearch.dto.CompoundSimpleSearchRequestDTO;
import ceu.biolab.cmm.msSearch.dto.RTSearchResponseDTO;
import ceu.biolab.cmm.msSearch.service.CompoundService;


/*
 *  Simple Search Adapter Service
 *   
 *  Provides search functionality for single-compound queries. Adapts requests to the new internal search engine.
 *  
 *  Uses:
 *   - SimpleSearchAdapterRequestDTO for input
 *   - CompoundSimpleSearchRequestDTO for internal search
 *   - RTSearchResponseDTO for internal search results
 *   - SimpleSearchAdapterResponseDTO for output
 */

@Service
public class SimpleSearchAdapterService {

    private final CompoundService compoundService; 

    public SimpleSearchAdapterService(CompoundService compoundService) {
        this.compoundService = compoundService;
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
        RTSearchResponseDTO adaptedResult = compoundService.findCompoundsByMz(adaptedRequest);
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
            if(db == LegacyDatabase.ALL || db == LegacyDatabase.MINE || db == LegacyDatabase.METLIN || db == LegacyDatabase.ALL_EXCEPT_MINE){
                throw new ResponseStatusException(HttpStatus.GONE, "Database "+ db.getValue() + " is no longer supported");
            }
        }

        if(request.getMetaboliteTypes() == LegacyMetaboliteType.ALL_INCLUDING_PEPTIDES){
            throw new ResponseStatusException(HttpStatus.GONE, "Peptide search is not supported anymore");
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
                    mass += 1.007276;
                    break;
                case NEGATIVE:
                    mass -= 1.007276;
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
        
        if(legacyMetaboliteType == LegacyMetaboliteType.ALL_EXCEPT_PEPTIDES){
            return MetaboliteType.ALL;
        } else if(legacyMetaboliteType == LegacyMetaboliteType.ONLY_LIPIDS){
            return MetaboliteType.ONLYLIPIDS;
        } else {
            throw new ResponseStatusException(HttpStatus.GONE, "Peptide search is not supported anymore");
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

        //Becouse we search for a single compound, we only return the first result.

        AnnotatedFeature result = adaptedResult.getMSFeatures().get(0);


        SimpleSearchAdapterResponseDTO response =  new SimpleSearchAdapterResponseDTO();

        response.setIdentifier(0);
        response.setEM(0.0);
        response.setName(null);
        response.setFormula(null);
        response.setAdduct(null);
        response.setAdducts(null);
        response.setMolecularWeight(0.0);
        response.setErrorPpm(null);
        response.setIonizationScore(null);
        response.setFinalScore(null);
        response.setCas(null);

        //Database related fields
        response.setKeggCompound(null);
        response.setKeggUri(null);
        response.setHmdbCompound(null);
        response.setHmdbUri(null);
        response.setLipidmapsCompound(null);
        response.setLipidmapsUri(null);
        response.setPubchemCompound(null);
        response.setPubchemUri(null);

        response.setPathways(null);
        response.setInChiKey(null);


        //not supported anymore
        response.setMetlinCompound(null);
        response.setMetlinUri(null);


        return response;
    }
}
