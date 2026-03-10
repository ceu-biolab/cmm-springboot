package ceu.biolab.cmm.metadata.service;

import ceu.biolab.cmm.metadata.dto.CcsAdductCatalogResponse;
import ceu.biolab.cmm.metadata.dto.CeMsBufferOption;
import ceu.biolab.cmm.metadata.dto.DatabaseStatsResponse;
import ceu.biolab.cmm.metadata.repository.MetadataRepository;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.adduct.AdductDefinition;
import ceu.biolab.cmm.shared.service.adduct.AdductService;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

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
}
