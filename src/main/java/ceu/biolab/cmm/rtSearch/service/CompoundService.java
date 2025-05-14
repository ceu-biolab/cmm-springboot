package ceu.biolab.cmm.rtSearch.service;

import ceu.biolab.cmm.rtSearch.dto.CompoundSimpleSearchRequestDTO;
import ceu.biolab.cmm.rtSearch.dto.RTSearchResponseDTO;
import ceu.biolab.cmm.rtSearch.repository.CompoundRepository;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompoundService {

    @Autowired
    private CompoundRepository compoundRepository;


    public RTSearchResponseDTO findCompoundsByMz(CompoundSimpleSearchRequestDTO request) {
        RTSearchResponseDTO response = new RTSearchResponseDTO();

        try {
            List<AnnotatedFeature> results = compoundRepository.annotateMSFeature(request.getMz(), request.getMzToleranceMode(), request.getTolerance(),
                    request.getIonizationMode(), request.getDetectedAdduct(), request.getFormulaTypeInt(), request.getAdductsString(), request.getDatabases(), request.getMetaboliteType());
            for (AnnotatedFeature feature : results) {
                response.addImFeature(feature);

                // -- Commented it out because it wasn't doing anything but throwing a warning
                // for(AnnotationsByAdduct annotationsByAdduct :feature.getAnnotationsByAdducts()){
                //     for(Annotation annotations : annotationsByAdduct.getAnnotations()){
                //         //annotations.getCompound().se
                //     }
                // }
            }

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error annotating MS features", e);
        }
    }

}