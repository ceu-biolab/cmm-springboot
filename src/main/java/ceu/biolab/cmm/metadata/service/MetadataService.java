package ceu.biolab.cmm.metadata.service;

import ceu.biolab.cmm.CEMSSearch.domain.CeIonizationModeMapper;
import ceu.biolab.cmm.CEMSSearch.domain.CePolarity;
import ceu.biolab.cmm.metadata.dto.CcsAdductCatalogResponse;
import ceu.biolab.cmm.metadata.dto.CeMsBufferOption;
import ceu.biolab.cmm.metadata.dto.CeMsCompoundOption;
import ceu.biolab.cmm.metadata.dto.CeMsConditionOptions;
import ceu.biolab.cmm.metadata.dto.CeMsOptionsResponse;
import ceu.biolab.cmm.metadata.dto.DatabaseStatsResponse;
import ceu.biolab.cmm.metadata.repository.MetadataRepository;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.adduct.AdductDefinition;
import ceu.biolab.cmm.shared.service.adduct.AdductService;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MetadataService {

    private final MetadataRepository metadataRepository;

    public MetadataService(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    public CcsAdductCatalogResponse getCcsAdductCatalog() {
        Set<AdductDefinition> positiveDefinitions = new LinkedHashSet<>();
        Set<AdductDefinition> negativeDefinitions = new LinkedHashSet<>();

        for (MetadataRepository.CcsAdductRow row : metadataRepository.findCcsAdductsInUse()) {
            IonizationMode mode = mapMode(row.ionizationModeId());
            if (mode == null) {
                continue;
            }

            Optional<AdductDefinition> definition = resolveByLegacyKey(mode, row.adductType());
            if (definition.isEmpty()) {
                continue;
            }

            if (mode == IonizationMode.POSITIVE) {
                positiveDefinitions.add(definition.get());
            } else if (mode == IonizationMode.NEGATIVE) {
                negativeDefinitions.add(definition.get());
            }
        }

        List<String> positive = AdductService.sortByPriority(positiveDefinitions, IonizationMode.POSITIVE).stream()
                .map(AdductDefinition::canonical)
                .toList();
        List<String> negative = AdductService.sortByPriority(negativeDefinitions, IonizationMode.NEGATIVE).stream()
                .map(AdductDefinition::canonical)
                .toList();

        return new CcsAdductCatalogResponse(positive, negative);
    }

    public List<CeMsBufferOption> getCeMsBuffers() {
        return metadataRepository.findCeMsBuffers().stream()
                .sorted(Comparator
                        .comparingInt((CeMsBufferOption option) -> bufferGroupPriority(option.code()))
                        .thenComparing(option -> normalizeBufferCode(option.code())))
                .toList();
    }

    public CeMsOptionsResponse getCeMsOptions(String bufferCode,
                                              Long temperature,
                                              String polarity,
                                              String ionizationMode) {
        String normalizedBufferCode = normalizeOptionalBufferCode(bufferCode);
        Integer polarityId = parsePolarityId(polarity);
        Integer ionizationModeId = parseIonizationModeId(ionizationMode);

        Map<String, CeMsConditionAccumulator> conditions = new LinkedHashMap<>();
        for (MetadataRepository.CeMsCompoundOptionRow row : metadataRepository.findCeMsMarkerOptions(
                normalizedBufferCode, temperature, polarityId, ionizationModeId)) {
            accumulatorFor(conditions, row).addMarker(row);
        }
        for (MetadataRepository.CeMsCompoundOptionRow row : metadataRepository.findCeMsRmtReferenceOptions(
                normalizedBufferCode, temperature, polarityId, ionizationModeId)) {
            accumulatorFor(conditions, row).addRmtReference(row);
        }

        List<CeMsConditionOptions> sortedConditions = conditions.values().stream()
                .sorted(Comparator
                        .comparingInt((CeMsConditionAccumulator condition) -> bufferGroupPriority(condition.bufferCode))
                        .thenComparing(condition -> normalizeBufferCode(condition.bufferCode))
                        .thenComparingLong(condition -> condition.temperature)
                        .thenComparingInt(condition -> condition.polarityId)
                        .thenComparingInt(condition -> condition.ionizationModeId))
                .map(CeMsConditionAccumulator::toDto)
                .toList();

        return new CeMsOptionsResponse(sortedConditions);
    }

    public DatabaseStatsResponse getDatabaseStats() {
        return metadataRepository.fetchDatabaseStats();
    }

    private Optional<AdductDefinition> resolveByLegacyKey(IonizationMode mode, String legacyKey) {
        if (legacyKey == null || legacyKey.isBlank()) {
            return Optional.empty();
        }
        return AdductService.definitionMap(mode).values().stream()
                .filter(definition -> definition.legacyKey().equalsIgnoreCase(legacyKey.trim()))
                .findFirst();
    }

    private IonizationMode mapMode(long ionizationModeId) {
        if (ionizationModeId == 1) {
            return IonizationMode.POSITIVE;
        }
        if (ionizationModeId == 2) {
            return IonizationMode.NEGATIVE;
        }
        return null;
    }

    private CeMsConditionAccumulator accumulatorFor(Map<String, CeMsConditionAccumulator> conditions,
                                                    MetadataRepository.CeMsCompoundOptionRow row) {
        String key = row.bufferCode() + "|" + row.temperature() + "|" + row.polarityId()
                + "|" + row.ionizationModeId();
        return conditions.computeIfAbsent(key, _ -> new CeMsConditionAccumulator(row));
    }

    private String normalizeOptionalBufferCode(String bufferCode) {
        if (bufferCode == null || bufferCode.isBlank()) {
            return null;
        }
        return normalizeBufferCode(bufferCode);
    }

    private Integer parsePolarityId(String polarity) {
        if (polarity == null || polarity.isBlank()) {
            return null;
        }
        try {
            return CePolarity.fromValue(polarity.trim()).getDatabaseValue();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    private Integer parseIonizationModeId(String ionizationMode) {
        if (ionizationMode == null || ionizationMode.isBlank()) {
            return null;
        }
        try {
            IonizationMode parsed = IonizationMode.valueOf(ionizationMode.trim().toUpperCase(Locale.ROOT));
            if (parsed == IonizationMode.NEUTRAL) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Neutral ionization mode is not supported.");
            }
            return CeIonizationModeMapper.toDatabaseValue(parsed);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unsupported ionization_mode: " + ionizationMode, ex);
        }
    }

    private String polarityLabel(int polarityId) {
        try {
            return CePolarity.fromDatabaseValue(polarityId).getLabel();
        } catch (IllegalArgumentException ex) {
            return Integer.toString(polarityId);
        }
    }

    private String ionizationModeLabel(int ionizationModeId) {
        return switch (ionizationModeId) {
            case 1 -> "Positive";
            case 2 -> "Negative";
            default -> Integer.toString(ionizationModeId);
        };
    }

    private int bufferGroupPriority(String bufferCode) {
        String normalized = normalizeBufferCode(bufferCode);
        if (normalized.startsWith("FORMIC_ACID_")) {
            return 0;
        }
        if (normalized.startsWith("AMMONIUM_ACETATE_")) {
            return 1;
        }
        if (normalized.startsWith("AMMONIUM_BICARBONATE_")) {
            return 2;
        }
        if (normalized.startsWith("ACETIC_ACID_")) {
            return 3;
        }
        return 4;
    }

    private String normalizeBufferCode(String bufferCode) {
        if (bufferCode == null) {
            return "";
        }
        return bufferCode.trim().toUpperCase(Locale.ROOT);
    }

    private final class CeMsConditionAccumulator {

        private final long bufferId;
        private final String bufferCode;
        private final String bufferDescription;
        private final long temperature;
        private final int polarityId;
        private final int ionizationModeId;
        private final Map<Long, CeMsCompoundOption> markerCompounds = new LinkedHashMap<>();
        private final Map<Long, CeMsCompoundOption> rmtReferenceCompounds = new LinkedHashMap<>();

        private CeMsConditionAccumulator(MetadataRepository.CeMsCompoundOptionRow row) {
            this.bufferId = row.bufferId();
            this.bufferCode = row.bufferCode();
            this.bufferDescription = row.bufferDescription();
            this.temperature = row.temperature();
            this.polarityId = row.polarityId();
            this.ionizationModeId = row.ionizationModeId();
        }

        private void addMarker(MetadataRepository.CeMsCompoundOptionRow row) {
            markerCompounds.putIfAbsent(row.compoundId(), new CeMsCompoundOption(row.compoundId(), row.compoundName()));
        }

        private void addRmtReference(MetadataRepository.CeMsCompoundOptionRow row) {
            rmtReferenceCompounds.putIfAbsent(row.compoundId(), new CeMsCompoundOption(row.compoundId(), row.compoundName()));
        }

        private CeMsConditionOptions toDto() {
            String polarityLabel = polarityLabel(polarityId);
            String ionizationModeLabel = ionizationModeLabel(ionizationModeId);
            String key = bufferCode + "|" + temperature + "|" + polarityLabel + "|" + ionizationModeLabel;

            return new CeMsConditionOptions(
                    key,
                    new CeMsBufferOption(Math.toIntExact(bufferId), bufferCode, bufferDescription),
                    temperature,
                    polarityLabel,
                    ionizationModeLabel,
                    List.copyOf(markerCompounds.values()),
                    List.copyOf(rmtReferenceCompounds.values()));
        }
    }
}
