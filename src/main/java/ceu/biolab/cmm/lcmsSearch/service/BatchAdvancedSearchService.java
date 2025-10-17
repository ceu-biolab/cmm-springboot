package ceu.biolab.cmm.lcmsSearch.service;

import ceu.biolab.cmm.lcmsSearch.dto.BatchAdvancedSearchRequestDTO;
import ceu.biolab.cmm.msSearch.dto.CompoundSimpleSearchRequestDTO;
import ceu.biolab.cmm.msSearch.dto.RTSearchResponseDTO;
import ceu.biolab.cmm.msSearch.service.CompoundService;
import ceu.biolab.cmm.scoreAnnotations.service.ScoreLipids;
import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.shared.domain.ExperimentParameters;
import ceu.biolab.cmm.shared.domain.ModifierType;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.adduct.AdductDefinition;
import ceu.biolab.cmm.shared.service.adduct.AdductService;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import ceu.biolab.cmm.shared.domain.msFeature.LCMSFeature;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BatchAdvancedSearchService {
    @Autowired
    private CompoundService compoundService;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BatchAdvancedSearchService.class);

    public List<AnnotatedFeature> annotateAndScoreCmpoundsByMz(BatchAdvancedSearchRequestDTO batchAdvancedRequest) {
        BatchAdvancedSearchRequestDTO request = Optional.ofNullable(batchAdvancedRequest)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required"));
        validateRequest(request);

        List<Double> mzs = request.getMz();
        List<Double> retentionTimes = request.getRetentionTimes();
        List<Map<Double, Double>> compositeSpectrumList = request.getCompositeSpectrum();
        Set<String> adducts = request.getAdductsString();

        List<AnnotatedFeature> allAnnotatedFeatures = new ArrayList<>();

        for (int i = 0; i < mzs.size(); i++) {
            double mz = mzs.get(i);
            double rt = retentionTimes.get(i);
            Map<Double, Double> compositeSpectrum = compositeSpectrumList.get(i);

            List<AdductDefinition> orderedDefinitions = resolveAdductDefinitions(request.getIonizationMode(), adducts);

            Set<String> canonicalAdducts = orderedDefinitions.stream()
                    .map(AdductDefinition::canonical)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            String detectedAdduct = AdductService.detectAdduct(
                            request.getIonizationMode(),
                            mz,
                            canonicalAdducts,
                            compositeSpectrum)
                    .map(AdductDefinition::canonical)
                    .orElse("");

            FormulaType formulaType;
            try {
                formulaType = FormulaType.resolveFormulaType(String.valueOf(request.getFormulaType()), request.isDeuterium());
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
            }

            // 2. Simple search with detected Adduct
            logger.info("detected adduct : {}", detectedAdduct);
            logger.info("adducts formatted : {}", request.getAdductsString());
            CompoundSimpleSearchRequestDTO compoundSimpleSearchRequestDTO = new CompoundSimpleSearchRequestDTO(mz,
                    request.getMzToleranceMode(), request.getTolerance(), request.getIonizationMode(),
                    canonicalAdducts, Optional.ofNullable(detectedAdduct).filter(s -> !s.isEmpty()),
                    Optional.of(formulaType), request.getDatabases(), request.getMetaboliteType());

            RTSearchResponseDTO response = compoundService.findCompoundsByMz(compoundSimpleSearchRequestDTO);
            List<AnnotatedFeature> annotatedFeatures = response.getMSFeatures();

            for (AnnotatedFeature feature : annotatedFeatures) {
                double mzValue = feature.getFeature().getMzValue();
                LCMSFeature lcmsFeature = new LCMSFeature(rt, mzValue);
                feature.setFeature(lcmsFeature);
            }

            //3. Score Annotations
            ExperimentParameters experimentParameters = new ExperimentParameters();
            experimentParameters.setIonMode(Optional.of(request.getIonizationMode()));
            ModifierType modifierType;
            try {
                modifierType = ModifierType.fromName(request.getModifiersType());
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown modifiers type: " + request.getModifiersType(), ex);
            }
            experimentParameters.setModifierType(Optional.of(modifierType));

            ScoreLipids.scoreLipidAnnotations(annotatedFeatures, Optional.of(experimentParameters));
            allAnnotatedFeatures.addAll(annotatedFeatures);
        }

        return allAnnotatedFeatures;
    }

    private void validateRequest(BatchAdvancedSearchRequestDTO request) {
        if (request.getMz() == null || request.getMz().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one m/z value is required.");
        }
        if (request.getMz().contains(null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "m/z values must not contain null entries.");
        }
        if (request.getRetentionTimes() == null || request.getRetentionTimes().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Retention times are required.");
        }
        if (request.getRetentionTimes().contains(null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Retention times must not contain null entries.");
        }
        if (request.getCompositeSpectrum() == null || request.getCompositeSpectrum().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Composite spectra are required.");
        }
        if (request.getCompositeSpectrum().stream().anyMatch(spectrum -> spectrum == null || spectrum.isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each composite spectrum must contain peaks.");
        }
        if (request.getCompositeSpectrum().stream()
                .filter(Objects::nonNull)
                .anyMatch(spectrum -> spectrum.keySet().contains(null) || spectrum.values().contains(null))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Composite spectra must not contain null m/z or intensity values.");
        }
        if (request.getMz().size() != request.getRetentionTimes().size()
                || request.getMz().size() != request.getCompositeSpectrum().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MZ, RT, and CompositeSpectra lists must have the same size.");
        }
        if (request.getMzToleranceMode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mzToleranceMode is required.");
        }
        if (request.getTolerance() == null || request.getTolerance() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tolerance must be non-negative.");
        }
        if (request.getIonizationMode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ionization mode is required.");
        }
        if (request.getAdductsString() == null || request.getAdductsString().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one adduct is required.");
        }
        if (request.getAdductsString().contains(null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adduct list must not contain null entries.");
        }
        if (request.getDatabases() == null || request.getDatabases().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one database must be provided.");
        }
        if (request.getDatabases().contains(null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Database list must not contain null entries.");
        }
        if (request.getMetaboliteType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Metabolite type is required.");
        }
        if (request.getFormulaType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formula type is required.");
        }
        if (request.getModifiersType() == null || request.getModifiersType().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Modifiers type is required.");
        }
    }

    private List<AdductDefinition> resolveAdductDefinitions(IonizationMode ionizationMode, Set<String> adducts) {
        try {
            return AdductService.sortByPriority(
                    adducts.stream()
                            .map(candidate -> AdductService.requireDefinition(ionizationMode, candidate))
                            .collect(Collectors.toCollection(LinkedHashSet::new)),
                    ionizationMode);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }
}
