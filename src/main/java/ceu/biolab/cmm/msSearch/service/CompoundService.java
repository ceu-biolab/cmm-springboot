package ceu.biolab.cmm.msSearch.service;

import ceu.biolab.cmm.msSearch.dto.CompoundSimpleSearchRequestDTO;
import ceu.biolab.cmm.msSearch.dto.RTSearchResponseDTO;
import ceu.biolab.cmm.msSearch.repository.CompoundRepository;

import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompoundService {

    @Autowired
    private CompoundRepository compoundRepository;


    /**
     * This methods obtains the compounds for a mz
     * @param request the Simple search request containing the MS parameters
     * @return a RT Search Response DTO which contains a list of features
     */
    public RTSearchResponseDTO findCompoundsByMz(CompoundSimpleSearchRequestDTO request) {
        RTSearchResponseDTO response = new RTSearchResponseDTO();

        try {
            List<AnnotatedFeature> results = compoundRepository.annotateMSFeature(request.getMz(), request.getMzToleranceMode(), request.getTolerance(),
                    request.getIonizationMode(), request.getDetectedAdduct(), request.getFormulaType(), request.getAdductsString(), request.getDatabases(), request.getMetaboliteType());
            for (AnnotatedFeature feature : results) {
                response.addImFeature(feature);
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error annotating MS features", e);
        }
    }

}