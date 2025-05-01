package ceu.biolab.cmm.batchAdvancedSearch.service;

import ceu.biolab.Adduct;
import ceu.biolab.IncorrectAdduct;
import ceu.biolab.cmm.batchAdvancedSearch.domain.ChemicalAlphabet;
import ceu.biolab.cmm.batchAdvancedSearch.dto.BatchAdvancedSearchRequestDTO;
import ceu.biolab.cmm.rtSearch.dto.CompoundSimpleSearchRequestDTO;
import ceu.biolab.cmm.rtSearch.dto.RTSearchResponseDTO;
import ceu.biolab.cmm.rtSearch.service.CompoundService;
import ceu.biolab.cmm.scoreAnnotations.dto.ScoreLipidRequest;
import ceu.biolab.cmm.scoreAnnotations.service.ScoreLipids;
import ceu.biolab.cmm.shared.domain.ExperimentParameters;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.ModifierType;
import ceu.biolab.cmm.shared.domain.adduct.AdductProcessing;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import ceu.biolab.cmm.shared.domain.msFeature.Annotation;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotationsByAdduct;
import ceu.biolab.cmm.shared.domain.msFeature.LCMSFeature;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xmlcml.molutil.ChemicalElement;

import java.util.*;

@Service
public class BatchAdvancedSearchService {
    @Autowired
    private CompoundService compoundService;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BatchAdvancedSearchService.class);

    public List<AnnotatedFeature> annotateAndScoreCmpoundsByMz(BatchAdvancedSearchRequestDTO batchAdvancedRequest) {
        try {
            // 1. We detect adduct from composite spectrum
            Set<String> adducts = batchAdvancedRequest.getAdductsString();
            Set<String> formattedAdducts = new HashSet<>();

            for (String adduct : adducts) {
                Adduct adductObj = AdductProcessing.getAdductFromString(adduct, batchAdvancedRequest.getIonizationMode(), batchAdvancedRequest.getMz());
                int chargeAdduct = adductObj.getAdductCharge();
                String formattedAdduct = "[" + adduct + "]" + chargeAdduct;
                formattedAdducts.add(formattedAdduct);
            }

            String detectedAdduct = AdductProcessing.detectAdductBasedOnCompositeSpectrum(
                    batchAdvancedRequest.getIonizationMode(),
                    batchAdvancedRequest.getMz(),
                    batchAdvancedRequest.getAdductsString(),
                    batchAdvancedRequest.getCompositeSpectrum()
            );

            //2. Simple search with detected Adduct
            CompoundSimpleSearchRequestDTO compoundSimpleSearchRequestDTO = new CompoundSimpleSearchRequestDTO(batchAdvancedRequest.getMz(),
                    batchAdvancedRequest.getMzToleranceMode(), batchAdvancedRequest.getTolerance(), batchAdvancedRequest.getIonizationMode(),
                    batchAdvancedRequest.getAdductsString(), Optional.of(detectedAdduct), batchAdvancedRequest.getDatabases(), batchAdvancedRequest.getMetaboliteType());

            RTSearchResponseDTO response = compoundService.findCompoundsByMz(compoundSimpleSearchRequestDTO);
            List<AnnotatedFeature> annotatedFeatures = response.getImFeatures();

            //3. Score Annotations
            ExperimentParameters experimentParameters = new ExperimentParameters();
            experimentParameters.setIonMode(Optional.of(batchAdvancedRequest.getIonizationMode()));
            ModifierType modifierType = ModifierType.fromName(batchAdvancedRequest.getModifiersType());
            experimentParameters.setModifierType(Optional.of(modifierType));

            for (AnnotatedFeature feature : annotatedFeatures) {
                double mzValue = feature.getFeature().getMzValue();
                LCMSFeature lcmsFeature = new LCMSFeature(batchAdvancedRequest.getRetentionTime(), mzValue);
                feature.setFeature(lcmsFeature);
            }

            ChemicalAlphabet chemAlphabetRequest = batchAdvancedRequest.getchemicalAlphabet();
            int formulaTypeIntValue = ChemicalAlphabet.dbValueForChemAlph(chemAlphabetRequest.name(), batchAdvancedRequest.isDeuterium());

            ScoreLipids.scoreLipidAnnotations(annotatedFeatures, Optional.of(experimentParameters));

            return annotatedFeatures;
        }catch (IncorrectAdduct e) {
            throw new RuntimeException(e);
        }
    }

}
