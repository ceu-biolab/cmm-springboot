package ceu.biolab.cmm.CEMSSearch.service;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ceu.biolab.cmm.CEMSSearch.domain.CeIonizationModeMapper;
import ceu.biolab.cmm.CEMSSearch.domain.CePolarity;
import ceu.biolab.cmm.CEMSSearch.dto.CeAnnotationDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CeAnnotationsByAdductDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CeFeatureAnnotationsDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CeFeatureDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsFeatureQueryDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsQueryResponseDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchRequestDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchResponseDTO;
import ceu.biolab.cmm.CEMSSearch.repository.CemsSearchRepository;
import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.compound.CompoundType;
import ceu.biolab.cmm.shared.service.adduct.AdductProcessing;

@Service
public class CemsSearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CemsSearchService.class);

    private final CemsSearchRepository repository;

    public CemsSearchService(CemsSearchRepository repository) {
        this.repository = repository;
    }

    public CemsSearchResponseDTO search(CemsSearchRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request payload cannot be null");
        }
        validateRequest(request);

        String bufferCode = normalizeBufferCode(request.getBufferCode());
        if (bufferCode == null) {
            throw new IllegalArgumentException("buffer_code is required");
        }

        CePolarity polarity = request.getPolarity();
        int polarityId = polarity.getDatabaseValue();

        IonizationMode ionizationMode = request.getIonizationMode();
        int ionizationModeId = CeIonizationModeMapper.toDatabaseValue(ionizationMode);

        Map<String, String> adductMap = AdductProcessing.getAdductMapByIonizationMode(ionizationMode);

        CemsSearchResponseDTO response = new CemsSearchResponseDTO();

        List<Double> mzValues = request.getMzValues();
        List<Double> effectiveMobilities = request.getEffectiveMobilities();
        Optional<Set<String>> allowedElements = parseChemicalAlphabet(request.getChemicalAlphabet());

        for (int i = 0; i < mzValues.size(); i++) {
            double mz = mzValues.get(i);
            double effMob = effectiveMobilities.get(i);

            CeFeatureDTO featureDTO = CeFeatureDTO.builder()
                    .mzValue(mz)
                    .effectiveMobility(effMob)
                    .intensity(null)
                    .build();

            CeFeatureAnnotationsDTO featureAnnotations = new CeFeatureAnnotationsDTO();
            featureAnnotations.setFeature(featureDTO);

            for (String adduct : request.getAdducts()) {
                String trimmedAdduct = adduct.trim();
                String adductValueRaw = adductMap.get(trimmedAdduct);
                if (adductValueRaw == null) {
                    LOGGER.warn("Skipping adduct '{}' because it is not supported for ionization mode {}", trimmedAdduct, ionizationMode);
                    continue;
                }

                double adductValue;
                try {
                    adductValue = Double.parseDouble(adductValueRaw);
                } catch (NumberFormatException ex) {
                    LOGGER.warn("Unable to parse adduct mass difference '{}' for adduct '{}'", adductValueRaw, trimmedAdduct, ex);
                    continue;
                }

                double neutralMass = AdductProcessing.getMassToSearch(mz, trimmedAdduct, adductValue);
                double massWindow = computeMassWindow(request.getMzToleranceMode(), request.getMzTolerance(), neutralMass);
                double mobilityWindow = computeMobilityWindow(effMob, request.getEffectiveMobilityTolerance());

                CemsFeatureQueryDTO query = CemsFeatureQueryDTO.builder()
                        .massLower(neutralMass - massWindow)
                        .massUpper(neutralMass + massWindow)
                        .mobilityLower(effMob - mobilityWindow)
                        .mobilityUpper(effMob + mobilityWindow)
                        .bufferCode(bufferCode)
                        .polarityId(polarityId)
                        .ionizationModeId(ionizationModeId)
                        .build();

                List<CemsQueryResponseDTO> candidates;
                try {
                    candidates = repository.findMatchingCompounds(query);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to read CE-MS search SQL", e);
                }
                candidates = deduplicateCandidates(candidates, neutralMass, effMob);

                CeAnnotationsByAdductDTO annotationsByAdduct = new CeAnnotationsByAdductDTO(trimmedAdduct);
                int rank = 1;
                for (CemsQueryResponseDTO candidate : candidates) {
                    Compound compound = toCompound(candidate);
                    if (!matchesAlphabet(compound, allowedElements)) {
                        continue;
                    }

                    Double massErrorPpm = computeMassErrorPpm(candidate.getMass(), neutralMass);
                    Double mzCalc = computeTheoreticalMz(candidate.getMass(), trimmedAdduct, ionizationMode);
                    Double mobilityErrorPct = computeMobilityErrorPct(candidate.getExperimentalEffMob(), effMob);

                    CeAnnotationDTO annotation = CeAnnotationDTO.builder()
                            .compound(compound)
                            .rank(rank++)
                            .massErrorPpm(massErrorPpm)
                            .mzCalc(mzCalc)
                            .neutralMassCalc(candidate.getMass())
                            .mobilityErrorPct(mobilityErrorPct)
                            .score(null)
                            .build();

                    annotationsByAdduct.addAnnotation(annotation);
                }

                featureAnnotations.addAnnotationsByAdduct(annotationsByAdduct);
            }

            response.addFeature(featureAnnotations);
        }

        return response;
    }

    private String normalizeBufferCode(String bufferCode) {
        if (bufferCode == null) {
            return null;
        }
        String normalized = bufferCode.trim().toUpperCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private void validateRequest(CemsSearchRequestDTO request) {
        if (request.getMzValues() == null || request.getEffectiveMobilities() == null) {
            throw new IllegalArgumentException("Both mz_values and effective_mobilities are required");
        }
        if (request.getMzValues().isEmpty()) {
            throw new IllegalArgumentException("At least one mz value must be provided");
        }
        if (request.getMzValues().size() != request.getEffectiveMobilities().size()) {
            throw new IllegalArgumentException("Number of mz values and effective mobilities must match");
        }
        if (request.getAdducts() == null || request.getAdducts().isEmpty()) {
            throw new IllegalArgumentException("At least one adduct must be provided");
        }
    }

    private double computeMassWindow(MzToleranceMode toleranceMode, double tolerance, double neutralMass) {
        if (toleranceMode == MzToleranceMode.PPM) {
            return Math.abs(neutralMass) * tolerance * 1e-6;
        } else if (toleranceMode == MzToleranceMode.MDA) {
            return tolerance * 0.001;
        }
        throw new IllegalArgumentException("Unsupported m/z tolerance mode: " + toleranceMode);
    }

    private double computeMobilityWindow(double effectiveMobility, double tolerancePercent) {
        double window = Math.abs(effectiveMobility) * (tolerancePercent * 0.01);
        return window;
    }

    private double rankingScore(CemsQueryResponseDTO candidate, double targetMass, double targetMobility) {
        double massDelta = candidate.getMass() != null
                ? Math.abs(candidate.getMass() - targetMass)
                : 1e9;
        double mobilityDelta = candidate.getExperimentalEffMob() != null
                ? Math.abs(candidate.getExperimentalEffMob() - targetMobility)
                : 1e9;
        return massDelta + mobilityDelta;
    }

    private Double computeMassErrorPpm(Double candidateMass, double targetMass) {
        if (candidateMass == null || targetMass == 0d) {
            return null;
        }
        return (candidateMass - targetMass) / targetMass * 1e6;
    }

    private Double computeTheoreticalMz(Double neutralMass, String adduct, IonizationMode ionizationMode) {
        if (neutralMass == null) {
            return null;
        }
        try {
            return AdductProcessing.getMassOfAdductFromMonoWeight(neutralMass, adduct, ionizationMode);
        } catch (RuntimeException ex) {
            LOGGER.warn("Unable to compute theoretical m/z for adduct '{}'", adduct, ex);
            return null;
        }
    }

    private Double computeMobilityErrorPct(Double candidateMobility, double targetMobility) {
        if (candidateMobility == null || targetMobility == 0d) {
            return null;
        }
        return (candidateMobility - targetMobility) / targetMobility * 100d;
    }

    private List<CemsQueryResponseDTO> deduplicateCandidates(List<CemsQueryResponseDTO> candidates,
                                                             double targetMass,
                                                             double targetMobility) {
        Map<Long, CemsQueryResponseDTO> bestCandidatePerCompound = new HashMap<>();
        Map<Long, Double> bestScorePerCompound = new HashMap<>();

        for (CemsQueryResponseDTO candidate : candidates) {
            long compoundId = candidate.getCompoundId();
            double score = rankingScore(candidate, targetMass, targetMobility);
            Double bestScore = bestScorePerCompound.get(compoundId);
            if (bestScore == null || score < bestScore) {
                bestScorePerCompound.put(compoundId, score);
                bestCandidatePerCompound.put(compoundId, candidate);
            }
        }

        List<CemsQueryResponseDTO> deduplicated = new java.util.ArrayList<>(bestCandidatePerCompound.values());
        deduplicated.sort(Comparator.comparingDouble(candidate -> rankingScore(candidate, targetMass, targetMobility)));
        return deduplicated;
    }

    private Compound toCompound(CemsQueryResponseDTO candidate) {
        Compound compound = new Compound();
        long candidateId = candidate.getCompoundId();
        try {
            compound.setCompoundId(Math.toIntExact(candidateId));
        } catch (ArithmeticException ex) {
            LOGGER.warn("Compound id {} exceeds integer range, truncating for response", candidateId);
            compound.setCompoundId((int) candidateId);
        }
        compound.setCasId(candidate.getCasId());
        compound.setCompoundName(candidate.getCompoundName());
        compound.setFormula(candidate.getFormula());
        compound.setMass(candidate.getMass() != null ? candidate.getMass() : 0d);
        compound.setChargeType(safeLongToInt(candidate.getChargeType()));
        compound.setChargeNumber(safeLongToInt(candidate.getChargeNumber()));

        String formulaTypeValue = candidate.getFormulaType();
        if (formulaTypeValue != null) {
            try {
                compound.setFormulaType(FormulaType.valueOf(formulaTypeValue.toUpperCase()));
            } catch (IllegalArgumentException ex) {
                LOGGER.warn("Unknown formula type '{}' for compound {}", formulaTypeValue, candidateId);
            }
        }

        Integer compoundTypeRaw = candidate.getCompoundType();
        CompoundType compoundType = null;
        if (compoundTypeRaw != null) {
            try {
                compoundType = CompoundType.fromDbValue(compoundTypeRaw);
            } catch (IllegalArgumentException ex) {
                LOGGER.warn("Unknown compound type {} for compound {}", compoundTypeRaw, candidateId);
            }
        }
        if (compoundType == null) {
            compoundType = CompoundType.NON_LIPID;
        }
        compound.setCompoundType(compoundType);
        compound.setLogP(candidate.getLogp());
        compound.setRtPred(candidate.getRtPred());
        compound.setInchi(candidate.getInchi());
        compound.setInchiKey(candidate.getInchiKey());
        compound.setSmiles(candidate.getSmiles());
        compound.setLipidType(candidate.getLipidType());
        compound.setNumChains(candidate.getNumChains());
        compound.setNumCarbons(candidate.getNumberCarbons());
        compound.setDoubleBonds(candidate.getDoubleBonds());
        compound.setBiologicalActivity(candidate.getBiologicalActivity());
        compound.setMeshNomenclature(candidate.getMeshNomenclature());
        compound.setIupacClassification(candidate.getIupacClassification());
        compound.setMol2(null);
        if (compound.getPathways() == null) {
            compound.setPathways(new HashSet<>());
        }
        if (compound.getLipidMapsClassifications() == null) {
            compound.setLipidMapsClassifications(new HashSet<>());
        }
        return compound;
    }

    private Optional<Set<String>> parseChemicalAlphabet(String alphabet) {
        if (alphabet == null || alphabet.isBlank()) {
            return Optional.empty();
        }
        String normalized = alphabet.trim().toUpperCase();
        if ("ALL".equals(normalized) || "ALLD".equals(normalized)) {
            return Optional.empty();
        }
        Set<String> elements = new LinkedHashSet<>();
        Matcher matcher = ELEMENT_PATTERN.matcher(normalized);
        while (matcher.find()) {
            elements.add(matcher.group(1));
        }
        return elements.isEmpty() ? Optional.empty() : Optional.of(elements);
    }

    private boolean matchesAlphabet(Compound compound, Optional<Set<String>> allowedElements) {
        if (allowedElements.isEmpty()) {
            return true;
        }
        Optional<Set<String>> compoundElements = compound.formulaElements();
        if (compoundElements.isEmpty()) {
            // Compounds without a formula are included by default for every alphabet.
            return true;
        }
        return allowedElements.get().containsAll(compoundElements.get());
    }

    private int safeLongToInt(Long value) {
        if (value == null) {
            return 0;
        }
        try {
            return Math.toIntExact(value);
        } catch (ArithmeticException ex) {
            LOGGER.warn("Value {} exceeds integer range, truncating", value);
            return value > 0 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }
    }

    private static final Pattern ELEMENT_PATTERN = Pattern.compile("([A-Z][a-z]?)");
}
