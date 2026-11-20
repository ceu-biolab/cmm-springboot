package ceu.biolab.cmm.integration.metadata;

import ceu.biolab.cmm.metadata.repository.MetadataRepository;
import ceu.biolab.cmm.metadata.service.MetadataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MetadataCacheIntegrationTest {
    @Autowired MetadataRepository repository;
    @Autowired MetadataService service;

    @Test
    void cachedCatalogFiltersMatchOriginalDatabaseFiltersForEveryCondition() {
        var conditions = service.getCeMsOptions(null, null, null, null).conditions();
        assertFalse(conditions.isEmpty());
        for (var condition : conditions) {
            int polarity = condition.polarity().equals("Direct") ? 1 : 2;
            int ionization = condition.ionizationMode().equals("Positive") ? 1 : 2;
            String normalizedBuffer = condition.buffer().code().trim().toUpperCase(java.util.Locale.ROOT);
            var markers = repository.findCeMsMarkerOptions(normalizedBuffer, condition.temperature(), polarity, ionization);
            var references = repository.findCeMsRmtReferenceOptions(normalizedBuffer, condition.temperature(), polarity, ionization);
            var filtered = service.getCeMsOptions(condition.buffer().code(), condition.temperature(),
                    condition.polarity(), condition.ionizationMode()).conditions();
            if (markers.isEmpty() && references.isEmpty()) {
                assertTrue(filtered.isEmpty());
                continue;
            }
            assertEquals(1, filtered.size());
            assertEquals(markers.stream().map(MetadataRepository.CeMsCompoundOptionRow::compoundId).distinct().toList(),
                    filtered.getFirst().markerCompounds().stream().map(option -> option.compoundId()).toList());
            assertEquals(references.stream().map(MetadataRepository.CeMsCompoundOptionRow::compoundId).distinct().toList(),
                    filtered.getFirst().rmtReferenceCompounds().stream().map(option -> option.compoundId()).toList());
        }
    }
}
