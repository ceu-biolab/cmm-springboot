package ceu.biolab.cmm.ccsSearch.service;

import ceu.biolab.cmm.ccsSearch.dto.CcsFeatureQueryDTO;
import ceu.biolab.cmm.ccsSearch.dto.CcsQueryResponseDTO;
import ceu.biolab.cmm.ccsSearch.dto.CcsSearchRequestDTO;
import ceu.biolab.cmm.ccsSearch.dto.CcsSearchResponseDTO;
import ceu.biolab.cmm.ccsSearch.repository.CcsSearchRepository;
import ceu.biolab.cmm.ccsSearch.domain.CcsToleranceMode;
import ceu.biolab.cmm.ccsSearch.domain.IMFeature;
import ceu.biolab.cmm.ccsSearch.domain.BufferGas;
import ceu.biolab.cmm.ccsSearch.domain.IMMSCompound;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotationsByAdduct;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.compound.Pathway;
import ceu.biolab.cmm.shared.domain.compound.CompoundType;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import ceu.biolab.cmm.shared.domain.msFeature.Annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

@Service
public class CcsSearchService {

    @Autowired
    private CcsSearchRepository ccsSearchRepository;

    public CcsSearchResponseDTO search(CcsSearchRequestDTO request) {
        if (request.getCcsValues().size() != request.getMzValues().size()) {
            throw new IllegalArgumentException("Number of CCS values and m/z values must be equal.");
        }
        int nFeatures = request.getCcsValues().size();
        MzToleranceMode mzToleranceMode = request.getMzToleranceMode();
        CcsToleranceMode ccsToleranceMode = request.getCcsToleranceMode();
        BufferGas bufferGas = request.getBufferGas();

        double absoluteCcsTolerance;
        if (ccsToleranceMode == CcsToleranceMode.PERCENTAGE) {
            absoluteCcsTolerance = request.getCcsTolerance() * 0.01;
        } else if (ccsToleranceMode == CcsToleranceMode.ABSOLUTE) {
            absoluteCcsTolerance = request.getCcsTolerance();
        } else {
            throw new IllegalArgumentException("Invalid tolerance mode: " + request.getCcsToleranceMode());
        }

        CcsSearchResponseDTO response = new CcsSearchResponseDTO();
        for (int i = 0; i < nFeatures; i++) 
        {
            double mz = request.getMzValues().get(i);
            double ccs = request.getCcsValues().get(i);
            IMFeature feature = new IMFeature(mz, ccs);
            AnnotatedFeature imAnnotatedFeature = new AnnotatedFeature(feature);

            // TODO: Dummy adduct. Replace with actual adducts from request. Also add IonMode check?
            ArrayList<String> adducts = new ArrayList<>(java.util.Arrays.asList("M+H"));
            ArrayList<Double> adductIonMassDifferences = new ArrayList<>(java.util.Arrays.asList(1.007276));
            for (int j = 0; j < adducts.size(); j++) {
                String adduct = adducts.get(j);
                double ionMassDifference = adductIonMassDifferences.get(j);

                double ccsDifference = ccs * absoluteCcsTolerance;
                double ccsLower = ccs - ccsDifference;
                double ccsUpper = ccs + ccsDifference;

                double neutralMass = mz - ionMassDifference;
                double mzDifference;
                if (mzToleranceMode == MzToleranceMode.PPM) {
                    mzDifference = mz * request.getMzTolerance() * 0.000001;
                } else if (mzToleranceMode == MzToleranceMode.MDA) {
                    mzDifference = request.getMzTolerance() * 0.001;
                } else {
                    throw new IllegalArgumentException("Invalid tolerance mode: " + request.getMzToleranceMode());
                }
                double massLower = neutralMass - mzDifference;
                double massUpper = neutralMass + mzDifference;

                CcsFeatureQueryDTO queryData = new CcsFeatureQueryDTO(ccsLower, ccsUpper, massLower, massUpper, bufferGas.toString(), adduct);
                try {
                    List<CcsQueryResponseDTO> queryResults = ccsSearchRepository.findMatchingCompounds(queryData);
                    // QueryResults may have duplicate results where the same compound is found with different pathways.
                    // We need to merge these results.
                    List<Annotation> annotations = new ArrayList<>();
                    for (CcsQueryResponseDTO queryResult : queryResults) {
                        Pathway pathway = new Pathway(queryResult.getPathwayId(), queryResult.getPathwayName(), queryResult.getPathwayMap());

                        boolean found = false;
                        for (Annotation annotation : annotations) {
                            Compound compound = annotation.getCompound();
                            // TODO remove mutation after refactoring Compound
                            if (compound instanceof IMMSCompound imCompound) {
                                if (imCompound.getCompoundId() == queryResult.getCompoundId()) {
                                    imCompound.addPathway(pathway);
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (!found) {
                            CompoundType compoundType = CompoundType.fromDbValue(queryResult.getCompoundType());
                            if (compoundType == null) {
                                compoundType = CompoundType.NON_LIPID;
                            }

                            IMMSCompound imCompound = IMMSCompound.builder()
                                    .compoundId(queryResult.getCompoundId())
                                    .compoundName(queryResult.getCompoundName())
                                    .mass(queryResult.getMonoisotopicMass())
                                    .dbCcs(queryResult.getDbCcs())
                                    .formula(queryResult.getFormula())
                                    // TODO formula type in compound shouldnt be a int
                                    //.formulaType(queryResult.getFormulaType())
                                    .compoundType(compoundType)
                                    .logP(queryResult.getLogP())
                                    .build();
                            imCompound.addPathway(pathway);
                            Annotation annotation = new Annotation(imCompound);
                            annotations.add(annotation);
                        }
                    }
                    //AnnotationsByAdduct annotations = new AnnotationsByAdduct(adduct, annotations);
                    AnnotationsByAdduct annotationsByAdduct = new AnnotationsByAdduct(adduct, annotations);
                    imAnnotatedFeature.addAnnotationByAdduct(annotationsByAdduct);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            response.addImFeature(imAnnotatedFeature);
        }

        return response;
    }
}
