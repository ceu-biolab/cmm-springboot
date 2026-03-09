package ceu.biolab.cmm.CEMSSearch.service;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ceu.biolab.cmm.CEMSSearch.domain.CeIonizationModeMapper;
import ceu.biolab.cmm.CEMSSearch.domain.CePolarity;
import ceu.biolab.cmm.CEMSSearch.domain.CemsCompoundMapper;
import ceu.biolab.cmm.CEMSSearch.domain.EffMobToleranceMode;
import ceu.biolab.cmm.CEMSSearch.dto.CeAnnotationDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CeAnnotationsByAdductDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CeFeatureAnnotationsDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CeFeatureDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsFeatureQueryDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsQueryResponseDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchRequestDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchResponseDTO;
import ceu.biolab.cmm.CEMSSearch.repository.CemsSearchRepository;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.adduct.AdductDefinition;
import ceu.biolab.cmm.shared.service.MassErrorTools;
import ceu.biolab.cmm.shared.service.MzToleranceConverter;
import ceu.biolab.cmm.shared.service.adduct.AdductService;
import ceu.biolab.cmm.shared.validation.MzToleranceLimits;

@Service
public class CemsSearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CemsSearchService.class);

    private final CemsSearchRepository repository;

    public CemsSearchService(CemsSearchRepository repository) {
        this.repository = repository;
    }

    public CemsSearchResponseDTO search(CemsSearchRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request payload cannot be null");
        }
        validateRequest(request);

        String bufferCode = normalizeBufferCode(request.getBufferCode());
        if (bufferCode == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "buffer_code is required");
        }

        Double temperatureValue = request.getTemperature();
        if (temperatureValue == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "temperature is required");
        }
        if (temperatureValue <= 0d) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "temperature must be greater than zero");
        }
        long temperature = Math.round(temperatureValue);

        CePolarity polarity = request.getPolarity();
        int polarityId = polarity.getDatabaseValue();

        IonizationMode ionizationMode = request.getIonizationMode();
        int ionizationModeId = CeIonizationModeMapper.toDatabaseValue(ionizationMode);

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
                AdductDefinition definition;
                try {
                    definition = AdductService.requireDefinition(ionizationMode, adduct.trim());
                } catch (IllegalArgumentException ex) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
                }
                double neutralMass = AdductService.neutralMassFromMz(mz, definition);
                double massWindow = computeMassWindow(request.getMzToleranceMode(), request.getMzTolerance(), neutralMass);
                double mobilityWindow = computeMobilityWindow(
                        effMob,
                        request.getEffectiveMobilityToleranceMode(),
                        request.getEffectiveMobilityTolerance());

                CemsFeatureQueryDTO query = CemsFeatureQueryDTO.builder()
                        .massLower(neutralMass - massWindow)
                        .massUpper(neutralMass + massWindow)
                        .mobilityLower(effMob - mobilityWindow)
                        .mobilityUpper(effMob + mobilityWindow)
                        .bufferCode(bufferCode)
                        .temperature(temperature)
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

                CeAnnotationsByAdductDTO annotationsByAdduct = new CeAnnotationsByAdductDTO(definition.canonical());
                int rank = 1;
                for (CemsQueryResponseDTO candidate : candidates) {
                    Compound compound;
                    try {
                        compound = CemsCompoundMapper.toCompound(candidate);
                    } catch (IllegalArgumentException ex) {
                        LOGGER.warn("Skipping CE-MS candidate {} due to invalid numeric fields: {}",
                                candidate.getCompoundId(), ex.getMessage());
                        continue;
                    }
                    if (!matchesAlphabet(compound, allowedElements)) {
                        continue;
                    }

                    Double massErrorPpm = MassErrorTools.computePpm(candidate.getMass(), neutralMass);
                    Double mzCalc = computeTheoreticalMz(candidate.getMass(), definition);
                    Double mobilityErrorPct = computeMobilityErrorPct(candidate.getExperimentalEffMob(), effMob);

                    CeAnnotationDTO annotation = CeAnnotationDTO.builder()
                            .compound(compound)
                            .rank(rank++)
                            .massErrorPpm(massErrorPpm)
                            .mzCalc(mzCalc)
                            .neutralMassCalc(candidate.getMass())
                            .mobilityErrorPct(mobilityErrorPct)
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both mz_values and effective_mobilities are required");
        }
        if (request.getMzValues().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one mz value must be provided");
        }
        if (request.getMzValues().size() != request.getEffectiveMobilities().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Number of mz values and effective mobilities must match");
        }
        if (request.getAdducts() == null || request.getAdducts().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one adduct must be provided");
        }
        if (request.getMzValues().stream().anyMatch(value -> value == null || value <= 0d)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mz_values must contain positive numbers only");
        }
        if (request.getEffectiveMobilities().stream().anyMatch(value -> value == null || value <= 0d)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "effective_mobilities must contain positive numbers only");
        }
        if (request.getMzTolerance() <= 0d) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mz_tolerance must be greater than zero");
        }
        if (request.getMzToleranceMode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mz_tolerance_mode is required");
        }
        if (MzToleranceLimits.exceedsLimit(request.getMzTolerance(), request.getMzToleranceMode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MzToleranceLimits.violationMessage("mz_tolerance", request.getMzToleranceMode()));
        }
        if (request.getEffectiveMobilityTolerance() <= 0d) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "eff_mob_tolerance must be greater than zero");
        }
        if (request.getEffectiveMobilityToleranceMode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "eff_mob_tolerance_mode is required");
        }
        if (request.getPolarity() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "polarity is required");
        }
        if (request.getIonizationMode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ionization mode is required");
        }
        if (request.getIonizationMode() == IonizationMode.NEUTRAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Neutral ionization mode is not supported.");
        }
    }

    private double computeMassWindow(MzToleranceMode toleranceMode, double tolerance, double neutralMass) {
        try {
            return MzToleranceConverter.toDaltons(toleranceMode, tolerance, Math.abs(neutralMass));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported m/z tolerance mode: " + toleranceMode, ex);
        }
    }

    private double computeMobilityWindow(double effectiveMobility,
                                         EffMobToleranceMode toleranceMode,
                                         double toleranceValue) {
        return switch (toleranceMode) {
            case PERCENTAGE -> Math.abs(effectiveMobility) * (toleranceValue * 0.01);
            case ABSOLUTE -> toleranceValue;
        };
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

    private Double computeTheoreticalMz(Double neutralMass, AdductDefinition definition) {
        if (neutralMass == null) {
            return null;
        }
        try {
            return AdductService.mzFromNeutralMass(neutralMass, definition);
        } catch (RuntimeException ex) {
            LOGGER.warn("Unable to compute theoretical m/z for adduct '{}'", definition.canonical(), ex);
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

    private static final Pattern ELEMENT_PATTERN = Pattern.compile("([A-Z][a-z]?)");
}
