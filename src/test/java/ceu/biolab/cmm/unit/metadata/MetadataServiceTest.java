package ceu.biolab.cmm.unit.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import ceu.biolab.cmm.metadata.dto.CeMsBufferOption;
import ceu.biolab.cmm.metadata.dto.CeMsConditionOptions;
import ceu.biolab.cmm.metadata.repository.MetadataRepository;
import ceu.biolab.cmm.metadata.service.MetadataService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetadataServiceTest {

    @Mock
    private MetadataRepository metadataRepository;

    @InjectMocks
    private MetadataService metadataService;

    @Test
    void getCeMsBuffersSortsByFamilyPriorityThenAlphabetically() {
        List<CeMsBufferOption> input = List.of(
                new CeMsBufferOption(9, "BORATE_20mM", "Unknown fallback family"),
                new CeMsBufferOption(4, "ACETIC_ACID_10PERCENT", "Acetic Acid 10%"),
                new CeMsBufferOption(2, "AMMONIUM_ACETATE_50mM", "Ammonium Acetate 50 mM"),
                new CeMsBufferOption(8, "AMMONIUM_BICARBONATE_50mM", "Ammonium Bicarbonate 50 mM"),
                new CeMsBufferOption(1, "FORMIC_ACID_1M", "Formic Acid 1 M"),
                new CeMsBufferOption(6, "AMMONIUM_BICARBONATE_20mM", "Ammonium Bicarbonate 20 mM"),
                new CeMsBufferOption(3, "FORMIC_ACID_0DOT1M", "Formic Acid 0.1 M"),
                new CeMsBufferOption(11, "AMMONIUM_ACETATE_35mM", "Ammonium Acetate 35 mM")
        );

        when(metadataRepository.findCeMsBuffers()).thenReturn(input);

        List<String> sortedCodes = metadataService.getCeMsBuffers().stream()
                .map(CeMsBufferOption::code)
                .toList();

        assertEquals(List.of(
                "FORMIC_ACID_0DOT1M",
                "FORMIC_ACID_1M",
                "AMMONIUM_ACETATE_35mM",
                "AMMONIUM_ACETATE_50mM",
                "AMMONIUM_BICARBONATE_20mM",
                "AMMONIUM_BICARBONATE_50mM",
                "ACETIC_ACID_10PERCENT",
                "BORATE_20mM"
        ), sortedCodes);
    }

    @Test
    void getCeMsOptionsGroupsMarkerAndRmtReferenceCompoundsByCondition() {
        List<MetadataRepository.CeMsCompoundOptionRow> markerRows = List.of(
                new MetadataRepository.CeMsCompoundOptionRow(
                        1, "FORMIC_ACID_1M", "Formic Acid 1 M", 20, 1, 1,
                        180838, "L-Methionine sulfone"),
                new MetadataRepository.CeMsCompoundOptionRow(
                        1, "FORMIC_ACID_1M", "Formic Acid 1 M", 20, 1, 1,
                        91854, "Hippuric acid")
        );
        List<MetadataRepository.CeMsCompoundOptionRow> rmtReferenceRows = List.of(
                new MetadataRepository.CeMsCompoundOptionRow(
                        1, "FORMIC_ACID_1M", "Formic Acid 1 M", 20, 1, 1,
                        180838, "L-Methionine sulfone"),
                new MetadataRepository.CeMsCompoundOptionRow(
                        1, "FORMIC_ACID_1M", "Formic Acid 1 M", 20, 1, 1,
                        73414, "Paracetamol")
        );

        when(metadataRepository.findCeMsMarkerOptions("FORMIC_ACID_1M", 20L, 1, 1)).thenReturn(markerRows);
        when(metadataRepository.findCeMsRmtReferenceOptions("FORMIC_ACID_1M", 20L, 1, 1)).thenReturn(rmtReferenceRows);

        List<CeMsConditionOptions> conditions = metadataService
                .getCeMsOptions("formic_acid_1m", 20L, "Direct", "Positive")
                .conditions();

        assertEquals(1, conditions.size());
        CeMsConditionOptions condition = conditions.getFirst();
        assertEquals("FORMIC_ACID_1M|20|Direct|Positive", condition.key());
        assertEquals("FORMIC_ACID_1M", condition.buffer().code());
        assertEquals("Direct", condition.polarity());
        assertEquals("Positive", condition.ionizationMode());
        assertEquals(List.of("L-Methionine sulfone", "Hippuric acid"),
                condition.markerCompounds().stream().map(option -> option.name()).toList());
        assertEquals(List.of("L-Methionine sulfone", "Paracetamol"),
                condition.rmtReferenceCompounds().stream().map(option -> option.name()).toList());
    }
}
