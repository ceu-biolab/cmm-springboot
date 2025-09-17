package ceu.biolab.cmm.unit.MSMSSearch.service;

import ceu.biolab.cmm.MSMSSearch.domain.Spectrum;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.msFeature.MSPeak;
import ceu.biolab.cmm.shared.domain.msFeature.ScoreType;
import ceu.biolab.cmm.shared.service.SpectrumScorer;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class SpectrumScorerMoreTests {

    @Test
    void padMSPeaks_mergesDuplicatesAndOrders() {
        Spectrum a = new Spectrum();
        a.getPeaks().addAll(Arrays.asList(
                new MSPeak(100.0, 10.0),
                new MSPeak(100.0, 15.0), // duplicate m/z should merge as max for A's map
                new MSPeak(200.0, 5.0)
        ));
        Spectrum b = new Spectrum();
        b.getPeaks().addAll(Arrays.asList(
                new MSPeak(150.0, 1.0),
                new MSPeak(150.0, 2.0) // duplicate m/z should sum for B's map
        ));

        SpectrumScorer scorer = new SpectrumScorer(MzToleranceMode.MDA, 100.0);
        var pair = scorer.padMSPeaks(a.getPeaks(), b.getPeaks());
        double[] vecA = pair.getLeft();
        double[] vecB = pair.getRight();

        // Expect 3 bins (100, 150, 200) in sorted order
        assertEquals(3, vecA.length);
        assertEquals(3, vecB.length);

        // After collapsing duplicates within tolerance and normalizing per spectrum to max=1
        // A at 100 -> 1.0; A at 200 -> ~0.333; B at 150 -> 1.0
        assertEquals(1.0, vecA[0], 1e-9);
        assertEquals(0.0, vecA[1], 1e-9);
        assertTrue(vecA[2] > 0.0 && vecA[2] < 0.5);

        assertEquals(0.0, vecB[0], 1e-9);
        assertEquals(1.0, vecB[1], 1e-9);
        assertEquals(0.0, vecB[2], 1e-9);
    }

    @Test
    void modifiedCosine_identicalSpectra_expectedToBeHigh_butShowsBuggyZero() {
        Spectrum a = new Spectrum();
        a.getPeaks().addAll(Arrays.asList(
                new MSPeak(100.0, 100.0),
                new MSPeak(200.0, 50.0)
        ));
        Spectrum b = new Spectrum();
        b.getPeaks().addAll(Arrays.asList(
                new MSPeak(100.0, 100.0),
                new MSPeak(200.0, 50.0)
        ));

        SpectrumScorer scorer = new SpectrumScorer(MzToleranceMode.MDA, 100.0);
        double score = scorer.compute(ScoreType.MODIFIED_COSINE, a.getPeaks(), b.getPeaks());

        assertEquals(1.0, score, 1e-9);
    }
}
