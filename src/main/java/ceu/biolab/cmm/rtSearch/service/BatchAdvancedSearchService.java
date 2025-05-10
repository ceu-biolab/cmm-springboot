package ceu.biolab.cmm.rtSearch.service;

import ceu.biolab.Adduct;
import ceu.biolab.IncorrectAdduct;
import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.rtSearch.dto.BatchAdvancedSearchRequestDTO;
import ceu.biolab.cmm.msSearch.dto.CompoundSimpleSearchRequestDTO;
import ceu.biolab.cmm.msSearch.dto.RTSearchResponseDTO;
import ceu.biolab.cmm.msSearch.service.CompoundService;
import ceu.biolab.cmm.scoreAnnotations.service.ScoreLipids;
import ceu.biolab.cmm.shared.domain.ExperimentParameters;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.ModifierType;
import ceu.biolab.cmm.shared.service.adduct.AdductProcessing;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import ceu.biolab.cmm.shared.domain.msFeature.LCMSFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;

@Service
public class BatchAdvancedSearchService {
    @Autowired
    private CompoundService compoundService;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BatchAdvancedSearchService.class);

    public List<AnnotatedFeature> annotateAndScoreCmpoundsByMz(BatchAdvancedSearchRequestDTO batchAdvancedRequest) {
        try {
            List<AnnotatedFeature> allAnnotatedFeatures = new ArrayList<>();

            // 1. We detect adduct from composite spectrum
            Set<String> adducts = batchAdvancedRequest.getAdductsString();
            Set<String> formattedAdducts = new HashSet<>();
            List<Double> mzs = batchAdvancedRequest.getMz();
            List<Double> retentionTimes = batchAdvancedRequest.getRetentionTimes();
            List<Map<Double,Double>> compositeSpectrumList = batchAdvancedRequest.getCompositeSpectrum();

            if (mzs.size() != retentionTimes.size() || mzs.size() != compositeSpectrumList.size()) {
                throw new IllegalArgumentException("MZ, RT, and CompositeSpectra lists must have the same size.");
            }

            for (int i = 0; i < mzs.size(); i++) {
                List<AnnotatedFeature> annotatedFeatures = new ArrayList<>();
                double mz = mzs.get(i);
                double rt = retentionTimes.get(i);
                Map<Double, Double> compositeSpectrum = compositeSpectrumList.get(i);

                for (String adduct : adducts) {
                    String formattedAdduct = AdductProcessing.formatAdductString(adduct, batchAdvancedRequest.getIonizationMode());
                    formattedAdducts.add(formattedAdduct);
                }

                String detectedAdduct = AdductProcessing.detectAdductBasedOnCompositeSpectrum(batchAdvancedRequest.getIonizationMode(),
                        mz, formattedAdducts, compositeSpectrum);

                FormulaType formulaType = FormulaType.resolveFormulaType(String.valueOf(batchAdvancedRequest.getFormulaType()), batchAdvancedRequest.isDeuterium());

                //2. Simple search with detected Adduct
                String formattedDetectedAdduct = null;
                if (detectedAdduct != null && !detectedAdduct.isEmpty()) {
                    formattedDetectedAdduct = AdductProcessing.formatAdductString(detectedAdduct, batchAdvancedRequest.getIonizationMode());
                }

                logger.info("detected adduct : {}", detectedAdduct);
                logger.info("detected adduct formatted : {}", formattedDetectedAdduct);
                CompoundSimpleSearchRequestDTO compoundSimpleSearchRequestDTO = new CompoundSimpleSearchRequestDTO(mz,
                        batchAdvancedRequest.getMzToleranceMode(), batchAdvancedRequest.getTolerance(), batchAdvancedRequest.getIonizationMode(),
                        batchAdvancedRequest.getAdductsString(), Optional.ofNullable(formattedDetectedAdduct), Optional.of(formulaType), batchAdvancedRequest.getDatabases(), batchAdvancedRequest.getMetaboliteType());

                RTSearchResponseDTO response = compoundService.findCompoundsByMz(compoundSimpleSearchRequestDTO);
                annotatedFeatures = response.getMSFeatures();

                //LCMS Features
                for (AnnotatedFeature feature : annotatedFeatures) {
                    double mzValue = feature.getFeature().getMzValue();
                    LCMSFeature lcmsFeature = new LCMSFeature(rt, mzValue);
                    feature.setFeature(lcmsFeature);
                }

                //3. Score Annotations
                ExperimentParameters experimentParameters = new ExperimentParameters();
                experimentParameters.setIonMode(Optional.of(batchAdvancedRequest.getIonizationMode()));
                ModifierType modifierType = ModifierType.fromName(batchAdvancedRequest.getModifiersType());
                experimentParameters.setModifierType(Optional.of(modifierType));

                ScoreLipids.scoreLipidAnnotations(annotatedFeatures, Optional.of(experimentParameters));
                allAnnotatedFeatures.addAll(annotatedFeatures);
            }
            return allAnnotatedFeatures;
        }catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }
}
