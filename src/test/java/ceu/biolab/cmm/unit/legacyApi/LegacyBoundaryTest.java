package ceu.biolab.cmm.unit.legacyApi;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

/** New backend request fields require an explicit compatibility mapping/default/omission decision. */
class LegacyBoundaryTest {
    @Test void backendRequestFieldInventoryRequiresReviewOnChange() throws Exception {
        Map<Class<?>, String> reviewed = Map.ofEntries(
            Map.entry(ceu.biolab.cmm.msSearch.dto.CompoundSimpleSearchRequestDTO.class, "mz mzToleranceMode tolerance ionizationMode detectedAdduct formulaType adductsString databases metaboliteType"),
            Map.entry(ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest.class, "compoundName formula databases metaboliteType exactName formulaType"),
            Map.entry(ceu.biolab.cmm.lcmsSearch.dto.BatchAdvancedSearchRequestDTO.class, "mzs mzToleranceMode tolerance ionizationMode detectedAdduct adductsString databases metaboliteType retentionTimes compositeSpectrum formulaType deuterium modifiersType"),
            Map.entry(ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchRequestDTO.class, "CIDEnergy precursorIonMZ tolerancePrecursorIon toleranceModePrecursorIon toleranceFragments toleranceModeFragments ionizationMode adducts fragmentsMZsIntensities scoreType spectrumSource"),
            Map.entry(ceu.biolab.cmm.CEMSSearch.dto.CemsSearchRequestDTO.class, "bufferCode polarity chemicalAlphabet ionizationMode adducts mzValues effectiveMobilities mzTolerance mzToleranceMode effectiveMobilityTolerance temperature effectiveMobilityToleranceMode"),
            Map.entry(ceu.biolab.cmm.CEMSSearch.dto.CemsRmtSearchRequestDTO.class, "masses tolerance toleranceMode relativeMigrationTimes rmtTolerance rmtToleranceMode bufferCode temperature polarity rmtReference chemicalAlphabet ionMode adducts"),
            Map.entry(ceu.biolab.cmm.CEMSMarkers.dto.CemsMarkersRequestDTO.class, "masses tolerance toleranceMode migrationTimes migrationTimeTolerance mtToleranceMode buffer temperature polarity marker markerTime capillaryLength capillaryVoltage chemicalAlphabet ionMode adducts"),
            Map.entry(ceu.biolab.cmm.CEMSMarkers.dto.CemsMarkersTwoRequestDTO.class, "masses tolerance toleranceMode migrationTimes migrationTimeTolerance mtToleranceMode buffer temperature polarity marker1 marker1Time marker2 marker2Time chemicalAlphabet ionMode adducts"),
            Map.entry(ceu.biolab.cmm.CEMSMarkers.dto.CemsRmtMarkersRequestDTO.class, "masses tolerance toleranceMode relativeMigrationTimes rmtTolerance rmtToleranceMode buffer temperature polarity rmtReference marker markerTime capillaryLength capillaryVoltage chemicalAlphabet ionMode adducts"),
            Map.entry(ceu.biolab.cmm.CEMSMarkers.dto.CemsRmtMarkersTwoRequestDTO.class, "masses tolerance toleranceMode relativeMigrationTimes rmtTolerance rmtToleranceMode buffer temperature polarity rmtReference marker1 marker1Time marker2 marker2Time chemicalAlphabet ionMode adducts"));
        for (var entry : reviewed.entrySet()) {
            Set<String> actual = Arrays.stream(entry.getKey().getDeclaredFields()).filter(f -> !f.isSynthetic() && !Modifier.isStatic(f.getModifiers()))
                    .map(java.lang.reflect.Field::getName).collect(Collectors.toSet());
            assertEquals(Set.of(entry.getValue().split(" ")), actual, entry.getKey().getName() + ": review legacy mapping and update this inventory deliberately");
        }
    }
    @Test void legacyDependenciesStayAtServiceAndDtoBoundaries() throws Exception {
        Path root = Path.of("src/main/java/ceu/biolab/cmm");
        try (var files = Files.walk(root)) {
            for (Path path : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                String source = Files.readString(path);
                if (path.startsWith(root.resolve("legacyApi"))) {
                    assertFalse(source.contains(".repository.") || source.contains("org.springframework.jdbc"), path.toString());
                    for(String line:source.lines().filter(l -> l.startsWith("import ceu.biolab.cmm.")).toList())
                        assertFalse(line.contains(".controller."), path + ": import services instead of controllers");
                } else {
                    assertFalse(source.contains("ceu.biolab.cmm.legacyApi"), path + ": core must not depend on legacy API");
                }
            }
        }
    }
}
