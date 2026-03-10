package ceu.biolab.cmm.unit.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import ceu.biolab.cmm.metadata.dto.CeMsBufferOption;
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
}
