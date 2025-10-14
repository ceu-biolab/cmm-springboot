package ceu.biolab.cmm.ccsSearch.service;

import ceu.biolab.IncorrectAdduct;
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
import ceu.biolab.cmm.shared.domain.adduct.AdductList;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.compound.Pathway;
import ceu.biolab.cmm.shared.domain.compound.CompoundType;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import ceu.biolab.cmm.shared.domain.msFeature.Annotation;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.shared.service.adduct.AdductProcessing;
import ceu.biolab.cmm.shared.service.adduct.AdductTransformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.text.Normalizer.Form;
import java.util.LinkedHashSet;
import java.util.Set;

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
        IonizationMode ionizationMode = request.getIonizationMode();

        List<String> requestedAdducts = request.getAdducts();
        Set<String> normalizedAdducts = new LinkedHashSet<>();
        if (requestedAdducts != null) {
            normalizedAdducts.addAll(requestedAdducts);
        }
        if (normalizedAdducts.isEmpty()) {
            if (ionizationMode == IonizationMode.NEGATIVE) {
                normalizedAdducts.add(AdductList.DEFAULT_ADDUCTS_NEGATIVE.get(0));
            } else {
                normalizedAdducts.add(AdductList.DEFAULT_ADDUCTS_POSITIVE.get(0));
            }
        }

        var adductMap = AdductProcessing.getAdductMapByIonizationMode(ionizationMode);
        List<String> effectiveAdducts = normalizedAdducts.stream()
                .filter(adductMap::containsKey)
                .toList();

        if (effectiveAdducts.isEmpty()) {
            throw new IllegalArgumentException("No valid adducts provided for ionization mode " + ionizationMode);
        }


        CcsSearchResponseDTO response = new CcsSearchResponseDTO();
        for (int i = 0; i < nFeatures; i++) 
        {
            double mz = request.getMzValues().get(i);
            double ccs = request.getCcsValues().get(i);
            IMFeature feature = new IMFeature(mz, ccs);
            AnnotatedFeature imAnnotatedFeature = new AnnotatedFeature(feature);

            for (String adduct : effectiveAdducts) {
                try {
                    AdductProcessing.getAdductFromString(adduct, ionizationMode, mz);
                } catch (IncorrectAdduct e) {
                    throw new IllegalArgumentException("Invalid adduct '" + adduct + "' for ion mode " + ionizationMode, e);
                }

                double neutralMass = AdductTransformer.getMonoisotopicMassFromMZ(mz, adduct, ionizationMode);

                double mzDifference;
                if (mzToleranceMode == MzToleranceMode.PPM) {
                    mzDifference = neutralMass * request.getMzTolerance() * 0.000001;
                } else if (mzToleranceMode == MzToleranceMode.MDA) {
                    mzDifference = request.getMzTolerance() * 0.001;
                } else {
                    throw new IllegalArgumentException("Invalid mz tolerance mode: " + request.getMzToleranceMode());
                }
                double massLower = neutralMass - mzDifference;
                double massUpper = neutralMass + mzDifference;

                double ccsDifference;
                if (ccsToleranceMode == CcsToleranceMode.PERCENTAGE) {
                    ccsDifference = ccs * (request.getCcsTolerance() / 100.0);
                } else if (ccsToleranceMode == CcsToleranceMode.ABSOLUTE) {
                    ccsDifference = request.getCcsTolerance();
                } else {
                    throw new IllegalArgumentException("Invalid CCS tolerance mode: " + request.getCcsToleranceMode());
                }
                double ccsLower = ccs - ccsDifference;
                double ccsUpper = ccs + ccsDifference;

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

                            String formula = queryResult.getFormula();
                            FormulaType formulaType = FormulaType.inferFromFormula(formula).orElse(null);

                            IMMSCompound.IMMSCompoundBuilder<?, ?> builder = IMMSCompound.builder()
                                    .compoundId(queryResult.getCompoundId())
                                    .compoundName(queryResult.getCompoundName())
                                    .mass(queryResult.getMonoisotopicMass())
                                    .dbCcs(queryResult.getDbCcs())
                                    .formula(formula)
                                    .formulaType(formulaType)
                                    .compoundType(compoundType)
                                    .logP(queryResult.getLogP());
                            IMMSCompound imCompound = builder.build();
                            imCompound.addPathway(pathway);
                            Annotation annotation = new Annotation(imCompound);
                            annotations.add(annotation);
                        }
                    }
                    AnnotationsByAdduct annotationsByAdduct = new AnnotationsByAdduct(adduct, annotations);
                    imAnnotatedFeature.addAnnotationByAdduct(annotationsByAdduct);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to execute CCS search query", e);
                }
            }

            response.addImFeature(imAnnotatedFeature);
        }

        return response;
    }
}
