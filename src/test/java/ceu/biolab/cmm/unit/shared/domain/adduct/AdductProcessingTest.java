package ceu.biolab.cmm.unit.shared.domain.adduct;

import ceu.biolab.cmm.shared.service.adduct.AdductProcessing;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AdductProcessingTest {

    @Test
    public void testFilterIsotopes() {
        Map<Double, Double> inputPeaks = new TreeMap<>();
        inputPeaks.put(400.3423, 307034.88);
        inputPeaks.put(401.34576, 73205.016);
        inputPeaks.put(402.3504, 15871.166);
        inputPeaks.put(403.35446, 2379.5325);
        inputPeaks.put(404.3498, 525.92053);

        Map<Double, Double> filteredPeaks = AdductProcessing.filterIsotopes(inputPeaks);

        assertTrue(filteredPeaks.containsKey(400.3423), "Peak 400.3432 should be present");
        assertFalse(filteredPeaks.containsKey(403.35446), "Peak 403.35446 should be present");
        assertFalse(filteredPeaks.containsKey(404.3498), "Peak 404.3498 should be present");
    }
}
