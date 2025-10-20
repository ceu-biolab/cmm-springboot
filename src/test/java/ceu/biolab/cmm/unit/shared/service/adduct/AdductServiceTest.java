package ceu.biolab.cmm.unit.shared.service.adduct;

import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.adduct.AdductDefinition;
import ceu.biolab.cmm.shared.service.adduct.AdductService;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AdductServiceTest {

    @Test
    void requireDefinitionLoadsCanonicalAdduct() {
        AdductDefinition definition = AdductService.requireDefinition(IonizationMode.POSITIVE, "[M+H]+");
        assertEquals("[M+H]+", definition.canonical());
        assertEquals(1, definition.multimer());
        assertEquals(1, definition.absoluteCharge());
        assertTrue(definition.isPositive());
        assertEquals(1.007276, definition.offset(), 1e-6);
    }

    @Test
    void neutralMassAndBackConversionAreConsistent() {
        double mz = 200.0d;
        AdductDefinition definition = AdductService.requireDefinition(IonizationMode.POSITIVE, "[M+2H]2+");
        double neutral = AdductService.neutralMassFromMz(mz, definition);
        assertEquals((mz * definition.absoluteCharge() - definition.offset()) / definition.multimer(), neutral, 1e-9);

        double reconstructedMz = AdductService.mzFromNeutralMass(neutral, definition);
        assertEquals(mz, reconstructedMz, 1e-9);
    }

    @Test
    void detectAdductReturnsCandidateWhenFragmentsMatch() {
        Map<Double, Double> peaks = new LinkedHashMap<>();
        peaks.put(200.0, 50.0);
        peaks.put(221.9819, 100.0);
        peaks.put(100.0, 20.0);

        Optional<AdductDefinition> detected = AdductService.detectAdduct(
                IonizationMode.POSITIVE,
                200.0,
                Set.of("[M+H]+", "[M+Na]+"),
                peaks
        );

        assertTrue(detected.isPresent());
        assertEquals("[M+H]+", detected.get().canonical());
    }

    @Test
    void filterIsotopesRemovesClosePeaks() {
        Map<Double, Double> peaks = new LinkedHashMap<>();
        peaks.put(100.0, 10.0);
        peaks.put(100.5, 5.0); // within isotope window
        peaks.put(105.0, 8.0);

        Map<Double, Double> filtered = AdductService.filterIsotopes(peaks);
        assertEquals(2, filtered.size());
        assertTrue(filtered.containsKey(100.0));
        assertTrue(filtered.containsKey(105.0));
    }
}
