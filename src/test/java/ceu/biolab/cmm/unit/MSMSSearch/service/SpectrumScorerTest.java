package ceu.biolab.cmm.unit.MSMSSearch.service;

import ceu.biolab.cmm.MSMSSearch.domain.ScoreType;
import ceu.biolab.cmm.MSMSSearch.domain.Spectrum;
import ceu.biolab.cmm.MSMSSearch.service.SpectrumScorer;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.msFeature.MSPeak;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class SpectrumScorerTest {

    @Test
    void normalizeIntensities_scalesToUnitMax() {
        Spectrum s = new Spectrum();
        s.getPeaks().addAll(Arrays.asList(
                new MSPeak(100.0, 100.0),
                new MSPeak(150.0, 50.0)
        ));

        SpectrumScorer scorer = new SpectrumScorer(MzToleranceMode.PPM, 10.0);
        scorer.normalizeIntensities(s);

        assertEquals(1.0, s.getPeaks().get(0).getIntensity(), 1e-9);
        assertEquals(0.5, s.getPeaks().get(1).getIntensity(), 1e-9);
    }

    @Test
    void cosineScore_identicalSpectra_returnsOne() {
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

        // 0.1 Da == 100 mDa
        SpectrumScorer scorer = new SpectrumScorer(MzToleranceMode.MDA, 100.0);
        double score = scorer.compute(ScoreType.COSINE, a, b);
        assertEquals(1.0, score, 1e-9);
    }

    @Test
    void cosineScore_withinDaTolerance_matchesPeaks() {
        Spectrum a = new Spectrum();
        a.getPeaks().addAll(Arrays.asList(
                new MSPeak(100.00, 100.0),
                new MSPeak(200.00, 50.0)
        ));
        Spectrum b = new Spectrum();
        b.getPeaks().addAll(Arrays.asList(
                new MSPeak(100.05, 80.0),   // within 0.1 Da
                new MSPeak(199.98, 50.0)    // within 0.1 Da
        ));

        // 0.1 Da == 100 mDa
        SpectrumScorer scorer = new SpectrumScorer(MzToleranceMode.MDA, 100.0);
        double score = scorer.compute(ScoreType.COSINE, a, b);
        assertTrue(score > 0.98, "Expected high similarity when peaks match within tolerance");
    }

    @Test
    void cosineScore_outsideDaTolerance_noMatch() {
        Spectrum a = new Spectrum();
        a.getPeaks().addAll(Arrays.asList(
                new MSPeak(100.00, 100.0)
        ));
        Spectrum b = new Spectrum();
        b.getPeaks().addAll(Arrays.asList(
                new MSPeak(100.50, 100.0)   // outside 0.1 Da tolerance
        ));

        // 0.1 Da == 100 mDa
        SpectrumScorer scorer = new SpectrumScorer(MzToleranceMode.MDA, 100.0);
        double score = scorer.compute(ScoreType.COSINE, a, b);
        assertEquals(0.0, score, 1e-9);
    }

    @Test
    void cosineScore_ppmTolerance_respectsWindow() {
        Spectrum a = new Spectrum();
        a.getPeaks().addAll(Arrays.asList(
                new MSPeak(400.0000, 100.0)
        ));
        Spectrum bWithin = new Spectrum();
        bWithin.getPeaks().addAll(Arrays.asList(
                new MSPeak(400.0012, 90.0)   // 3 ppm shift
        ));
        Spectrum bOutside = new Spectrum();
        bOutside.getPeaks().addAll(Arrays.asList(
                new MSPeak(400.0100, 90.0)   // 25 ppm shift
        ));

        SpectrumScorer scorerTight = new SpectrumScorer(MzToleranceMode.PPM, 5.0);
        double scoreWithin = scorerTight.compute(ScoreType.COSINE, a, bWithin);
        assertTrue(scoreWithin > 0.9, "Expected match within 5 ppm tolerance");

        SpectrumScorer scorerTight2 = new SpectrumScorer(MzToleranceMode.PPM, 5.0);
        double scoreOutside = scorerTight2.compute(ScoreType.COSINE, a, bOutside);
        assertEquals(0.0, scoreOutside, 1e-9, "Expected no match outside 5 ppm tolerance");
    }
}
