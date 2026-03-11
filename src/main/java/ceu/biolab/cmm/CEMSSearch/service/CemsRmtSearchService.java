package ceu.biolab.cmm.CEMSSearch.service;

import ceu.biolab.cmm.CEMSSearch.domain.CeIonizationModeMapper;
import ceu.biolab.cmm.CEMSSearch.domain.CePolarity;
import ceu.biolab.cmm.CEMSSearch.domain.CemsCompoundMapper;
import ceu.biolab.cmm.CEMSSearch.domain.RmtToleranceMode;
import ceu.biolab.cmm.CEMSSearch.dto.CeAnnotationDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CeAnnotationsByAdductDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CeFeatureAnnotationsDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CeFeatureDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsQueryResponseDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsRmtFeatureQueryDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsRmtSearchRequestDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchResponseDTO;
import ceu.biolab.cmm.CEMSSearch.repository.CemsRmtSearchRepository;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.adduct.AdductDefinition;
import ceu.biolab.cmm.shared.service.MassErrorTools;
import ceu.biolab.cmm.shared.service.MzToleranceConverter;
import ceu.biolab.cmm.shared.service.adduct.AdductService;
import ceu.biolab.cmm.shared.validation.MzToleranceLimits;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CemsRmtSearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CemsRmtSearchService.class);

    private final CemsRmtSearchRepository repository;

    public CemsRmtSearchService(CemsRmtSearchRepository repository) {
        this.repository = repository;
    }

    public CemsSearchResponseDTO search(CemsRmtSearchRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request payload cannot be null");
        }
        validateRequest(request);

        String bufferCode = normalizeBufferCode(request.getBufferCode());
        if (bufferCode == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "buffer is required");
        }

        if (request.getTemperature() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "temperature is required");
        }
        if (request.getTemperature() <= 0d) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "temperature must be greater than zero");
        }
        long temperature = Math.round(request.getTemperature());

        CePolarity polarity = request.getPolarity();
        int polarityId = polarity.getDatabaseValue();

        IonizationMode ionizationMode = request.getIonMode();
        int ionizationModeId = CeIonizationModeMapper.toDatabaseValue(ionizationMode);

        OptionalLong referenceIdOptional = repository.findReferenceCompoundId(request.getRmtReference());
        if (referenceIdOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown rmt_reference: " + request.getRmtReference());
        }
        long referenceCompoundId = referenceIdOptional.getAsLong();

        CemsSearchResponseDTO response = new CemsSearchResponseDTO();

        List<Double> mzValues = request.getMasses();
        List<Double> rmtValues = request.getRelativeMigrationTimes();
        Optional<Set<String>> allowedElements = parseChemicalAlphabet(request.getChemicalAlphabet());

        for (int i = 0; i < mzValues.size(); i++) {
            double mz = mzValues.get(i);
            double rmt = rmtValues.get(i);

            CeFeatureDTO featureDTO = CeFeatureDTO.builder()
                    .mzValue(mz)
                    .effectiveMobility(0d)
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
                double massWindow = computeMassWindow(request.getToleranceMode(), request.getTolerance(), neutralMass);
                double rmtWindow = computeRmtWindow(request.getRmtToleranceMode(), request.getRmtTolerance(), rmt);

                CemsRmtFeatureQueryDTO query = CemsRmtFeatureQueryDTO.builder()
                        .massLower(neutralMass - massWindow)
                        .massUpper(neutralMass + massWindow)
                        .rmtLower(rmt - rmtWindow)
                        .rmtUpper(rmt + rmtWindow)
                        .bufferCode(bufferCode)
                        .polarityId(polarityId)
                        .ionizationModeId(ionizationModeId)
                        .temperature(temperature)
                        .referenceCompoundId(referenceCompoundId)
                        .build();

                List<CemsQueryResponseDTO> candidates;
                try {
                    candidates = repository.findMatchingCompounds(query);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to read CE-MS RMT search SQL", e);
                }

                double targetMass = neutralMass;
                double targetRmt = rmt;
                candidates = deduplicateCandidates(candidates, targetMass, targetRmt);

                CeAnnotationsByAdductDTO annotationsByAdduct = new CeAnnotationsByAdductDTO(definition.canonical());
                int rank = 1;
                for (CemsQueryResponseDTO candidate : candidates) {
                    Compound compound;
                    try {
                        compound = CemsCompoundMapper.toCompound(candidate);
                    } catch (IllegalArgumentException ex) {
                        LOGGER.warn("Skipping CE-MS RMT candidate {} due to invalid numeric fields: {}",
                                candidate.getCompoundId(), ex.getMessage());
                        continue;
                    }
                    if (!matchesAlphabet(compound, allowedElements)) {
                        continue;
                    }

                    Double massErrorPpm = MassErrorTools.computePpm(candidate.getMass(), targetMass);
                    Double mzCalc = computeTheoreticalMz(candidate.getMass(), definition);
                    Double rmtErrorPct = computeRmtErrorPct(candidate.getRelativeMt(), targetRmt);

                    CeAnnotationDTO annotation = CeAnnotationDTO.builder()
                            .compound(compound)
                            .rank(rank++)
                            .massErrorPpm(massErrorPpm)
                            .mzCalc(mzCalc)
                            .neutralMassCalc(candidate.getMass())
                            .mobilityErrorPct(null)
                            .rmtErrorPct(rmtErrorPct)
                            .relativeMt(candidate.getRelativeMt())
                            .absoluteMt(candidate.getAbsoluteMt())
                            .build();

                    annotationsByAdduct.addAnnotation(annotation);
                }

                featureAnnotations.addAnnotationsByAdduct(annotationsByAdduct);
            }

            response.addFeature(featureAnnotations);
        }

        return response;
    }

    private void validateRequest(CemsRmtSearchRequestDTO request) {
        if (request.getMasses() == null || request.getMasses().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "masses must contain at least one value");
        }
        if (request.getRelativeMigrationTimes() == null || request.getRelativeMigrationTimes().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rmt must contain at least one value");
        }
        if (request.getMasses().size() != request.getRelativeMigrationTimes().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Number of masses and rmt values must match");
        }
        if (request.getAdducts() == null || request.getAdducts().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one adduct must be provided");
        }
        if (request.getRmtReference() == null || request.getRmtReference().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rmt_reference is required");
        }
        if (request.getMasses().stream().anyMatch(value -> value == null || value <= 0d)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "masses must contain positive numbers only");
        }
        if (request.getRelativeMigrationTimes().stream().anyMatch(value -> value == null || value <= 0d)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rmt must contain positive numbers only");
        }
        if (request.getTolerance() <= 0d) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tolerance must be greater than zero");
        }
        if (request.getToleranceMode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tolerance_mode is required");
        }
        if (MzToleranceLimits.exceedsLimit(request.getTolerance(), request.getToleranceMode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MzToleranceLimits.violationMessage("tolerance", request.getToleranceMode()));
        }
        if (request.getRmtTolerance() <= 0d) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rmt_tolerance must be greater than zero");
        }
        if (request.getRmtToleranceMode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rmt_tolerance_mode is required");
        }
        if (request.getPolarity() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "polarity is required");
        }
        if (request.getIonMode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ionization mode is required");
        }
        if (request.getIonMode() == IonizationMode.NEUTRAL) {
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

    private double computeRmtWindow(RmtToleranceMode toleranceMode, double tolerance, double baseRmt) {
        return switch (toleranceMode) {
            case PERCENTAGE -> Math.abs(baseRmt) * (tolerance * 0.01d);
            case ABSOLUTE -> tolerance;
        };
    }

    private List<CemsQueryResponseDTO> deduplicateCandidates(List<CemsQueryResponseDTO> candidates,
                                                             double targetMass,
                                                             double targetRmt) {
        Map<Long, CemsQueryResponseDTO> bestCandidatePerCompound = new HashMap<>();
        Map<Long, Double> bestScorePerCompound = new HashMap<>();

        for (CemsQueryResponseDTO candidate : candidates) {
            long compoundId = candidate.getCompoundId();
            double score = rankingScore(candidate, targetMass, targetRmt);
            Double bestScore = bestScorePerCompound.get(compoundId);
            if (bestScore == null || score < bestScore) {
                bestScorePerCompound.put(compoundId, score);
                bestCandidatePerCompound.put(compoundId, candidate);
            }
        }

        List<CemsQueryResponseDTO> deduplicated = new java.util.ArrayList<>(bestCandidatePerCompound.values());
        deduplicated.sort(java.util.Comparator.comparingDouble(candidate -> rankingScore(candidate, targetMass, targetRmt)));
        return deduplicated;
    }

    private double rankingScore(CemsQueryResponseDTO candidate, double targetMass, double targetRmt) {
        double massDelta = candidate.getMass() != null
                ? Math.abs(candidate.getMass() - targetMass)
                : 1e9;
        double rmtDelta = candidate.getRelativeMt() != null
                ? Math.abs(candidate.getRelativeMt() - targetRmt)
                : 1e9;
        return massDelta + rmtDelta;
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

    private Double computeRmtErrorPct(Double candidateRmt, double targetRmt) {
        if (candidateRmt == null || targetRmt == 0d) {
            return null;
        }
        return (candidateRmt - targetRmt) / targetRmt * 100d;
    }

    private String normalizeBufferCode(String bufferCode) {
        if (bufferCode == null) {
            return null;
        }
        String normalized = bufferCode.trim().toUpperCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private Optional<Set<String>> parseChemicalAlphabet(String alphabet) {
        if (alphabet == null || alphabet.isBlank()) {
            return Optional.empty();
        }
        String normalized = alphabet.trim().toUpperCase(Locale.ROOT);
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
            return true;
        }
        return allowedElements.get().containsAll(compoundElements.get());
    }

    private static final Pattern ELEMENT_PATTERN = Pattern.compile("([A-Z][a-z]?)");
}
