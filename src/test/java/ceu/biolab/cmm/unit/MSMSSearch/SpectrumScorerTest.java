package ceu.biolab.cmm.unit.MSMSSearch;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;

import ceu.biolab.cmm.MSMS.domain.Spectrum;
import ceu.biolab.cmm.MSMS.service.SpectrumScorer;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.msFeature.MSPeak;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SpectrumScorerTest {

    private SpectrumScorer scorer;

    @BeforeEach
    void setUp() {
        // We pick Da mode for simplicity and tolValue=0.1 (won't matter for these tests)
        scorer = new SpectrumScorer(MzToleranceMode.MDA, 0.5);
    }

    @Test
    void testNormalizeSpectrum() {
        // 1) Build a spectrum with intensities [2,4,6]
        Spectrum raw = new Spectrum(List.of(
                new MSPeak(100.0, 2.0),
                new MSPeak(200.0, 4.0),
                new MSPeak(300.0, 6.0)
        ));
        // 2) Normalize
        scorer.normalizeIntensities(raw);
        Spectrum norm = raw;
        // 3) Check max = 1.0
        double max = norm.getPeaks()
                .stream()
                .mapToDouble(MSPeak::getIntensity)
                .max()
                .orElseThrow();
        assertEquals(1.0, max, 1e-9);
        // 4) Check each value scaled proportionally
        assertAll("normalized intensities",
                () -> assertEquals(2.0 / 6.0, norm.getPeaks().get(0).getIntensity(), 1e-9),
                () -> assertEquals(4.0 / 6.0, norm.getPeaks().get(1).getIntensity(), 1e-9),
                () -> assertEquals(6.0 / 6.0, norm.getPeaks().get(2).getIntensity(), 1e-9)
        );
    }


    @Test
    void testCosineScoreOnSimpleCase() {
        // A = [1,2], B = [1,0] → expected cosine = (1*1+2*0)/(√5 * 1)=1/√5≈0.4472
        Spectrum specA = new Spectrum(List.of(
                new MSPeak(100.0, 1.0),
                new MSPeak(200.0, 2.0)
        ));
        Spectrum specB = new Spectrum(List.of(
                new MSPeak(100.0, 1.0)
                // no 200 m/z MSPeak → padding to 0
        ));

        double score = scorer.cosineScore(specA, specB);
        assertEquals(1.0 / Math.sqrt(5.0), score, 1e-4,
                "cosine([1,2], [1,0]) should be 1/√5");
    }

    @Test
    void testPaddingNormalizedSpectra() {
        // 1) Build raw spectra
        Spectrum spec1 = new Spectrum(List.of(
                new MSPeak(74.0964258, 0.6203296461),
                new MSPeak(84.08077574, 5.723794737),
                new MSPeak(88.11207587, 15.92527652),
                new MSPeak(182.0576709, 1.229660568),
                new MSPeak(184.073321, 1.357979413),
                new MSPeak(225.2576775, 3.670435487),
                new MSPeak(267.2682422, 7.879446941),
                new MSPeak(283.2995423, 2.132216341),
                new MSPeak(285.2788068, 3.005365091),
                new MSPeak(462.3342867, 1.010024938),
                new MSPeak(464.3499368, 4.88356222),
                new MSPeak(563.5397726, 1.035560548),
                new MSPeak(565.5554226, 3.403229444),
                new MSPeak(661.5166678, 1.713527115),
                new MSPeak(730.6109025, 1.379876098),
                new MSPeak(748.6214672, 8.370506766)
        ));

        Spectrum spec2 = new Spectrum(List.of(
                new MSPeak(88.11207587, 15.92527652),
                new MSPeak(182.0576709, 1.229660568),
                new MSPeak(184.073321, 1.357979413),
                new MSPeak(249.2576775, 1.069886032),
                new MSPeak(267.2682422, 7.879446941),
                new MSPeak(283.2995423, 2.132216341),
                new MSPeak(285.2788068, 3.005365091),
                new MSPeak(462.3342867, 1.010024938),
                new MSPeak(464.3499368, 4.88356222),
                new MSPeak(482.3605015, 2.341503307),
                new MSPeak(563.5397726, 1.035560548),
                new MSPeak(565.5554226, 3.403229444),
                new MSPeak(661.5166678, 1.713527115),
                new MSPeak(748.6214672, 8.370506766)
        ));

        // 2) Normalize in place
        SpectrumScorer scorer = new SpectrumScorer(MzToleranceMode.MDA, 0.5);
        scorer.normalizeIntensities(spec1);
        scorer.normalizeIntensities(spec2);

        // 3) Pad spectra
        Pair<double[], double[]> padded = scorer.padMSPeaks(spec1, spec2);

        double[] vecA = padded.getLeft();
        for (int i = 0; i < vecA.length; i++) {
            System.out.println("vecA[" + i + "] = " + vecA[i]);
        }
        double[] vecB = padded.getRight();
        for (int i = 0; i < vecB.length; i++) {
            System.out.println("vecB[" + i + "] = " + vecB[i]);
        }
        // Expected length = 18 unique m/z bins
        assertEquals(18, vecA.length);
        assertEquals(18, vecB.length);

        // 4) Expected normalized & padded intensities
        double[] expectedA = {
                0.03895251962004864, 0.35941572065085875, 1.0, 0.07721439351183083,
                0.085271951874403, 0.23047860314327528, 0.49477614602826375, 0.13388881118153417,
                0.18871666606389323, 0.06342275669320685, 0.30665478328535795, 0.06502622084454707,
                0.2136998650997364, 0.10759795051897786, 0.08664691606874528, 0.5256113923979764,
                0.0, 0.0
        };
        double[] expectedB = {
                0.0, 0.0, 1.0, 0.07721439351183083,
                0.085271951874403, 0.0, 0.49477614602826375, 0.13388881118153417,
                0.18871666606389323, 0.06342275669320685, 0.30665478328535795, 0.06502622084454707,
                0.2136998650997364, 0.10759795051897786, 0.0, 0.5256113923979764,
                0.14703062166985845, 0.0671816298232792
        };

        for (int i = 0; i < vecA.length; i++) {
            assertEquals(expectedA[i], vecA[i], 1e-6, "Mismatch at index " + i);
            assertEquals(expectedB[i], vecB[i], 1e-6, "Mismatch at index " + i);
        }
    }

    @Test
    void testCosineScore() {
        // 1) Build raw spectra
        Spectrum spec1 = new Spectrum(List.of(
                // Lista A
                new MSPeak(100.0, 3.50),
                new MSPeak(150.0, 5.10),
                new MSPeak(200.0, 2.20),
                new MSPeak(250.0, 6.80),
                new MSPeak(300.0, 1.90),
                new MSPeak(350.0, 4.30),
                new MSPeak(400.0, 7.00),
                new MSPeak(450.0, 3.30),
                new MSPeak(500.0, 5.50),
                new MSPeak(550.0, 2.70)

        ));

        Spectrum spec2 = new Spectrum(List.of(
                // Lista B
                new MSPeak(100.5, 3.65),
                new MSPeak(149.8, 4.98),
                new MSPeak(199.7, 2.30),
                new MSPeak(250.2, 6.92),
                new MSPeak(300.4, 1.75),
                new MSPeak(349.6, 4.15),
                new MSPeak(400.3, 7.10),
                new MSPeak(450.1, 3.45),
                new MSPeak(499.9, 5.40),
                new MSPeak(550.5, 2.85)

        ));
        double score = scorer.cosineScore(spec1, spec2);
        assertEquals(0.9996, score, 1e-4, "Cosine score between spec1 and spec2 should be approximately 0.9424");
    }

    @Test
    void testModiCosineScore() {
        // 1) Build raw spectra
        Spectrum spec1 = new Spectrum(List.of(
                new MSPeak(74.0964258, 0.6203296461),
                new MSPeak(84.08077574, 5.723794737),
                new MSPeak(88.11207587, 15.92527652),
                new MSPeak(182.0576709, 1.229660568),
                new MSPeak(184.073321, 1.357979413),
                new MSPeak(225.2576775, 3.670435487),
                new MSPeak(267.2682422, 7.879446941),
                new MSPeak(283.2995423, 2.132216341),
                new MSPeak(285.2788068, 3.005365091),
                new MSPeak(462.3342867, 1.010024938),
                new MSPeak(464.3499368, 4.88356222),
                new MSPeak(563.5397726, 1.035560548),
                new MSPeak(565.5554226, 3.403229444),
                new MSPeak(661.5166678, 1.713527115),
                new MSPeak(730.6109025, 1.379876098),
                new MSPeak(748.6214672, 8.370506766)

        ));

        Spectrum spec2 = new Spectrum(List.of(
                new MSPeak(88.11207587, 15.92527652),
                new MSPeak(182.0576709, 1.229660568),
                new MSPeak(184.073321, 1.357979413),
                new MSPeak(249.2576775, 1.069886032),
                new MSPeak(267.2682422, 7.879446941),
                new MSPeak(283.2995423, 2.132216341),
                new MSPeak(285.2788068, 3.005365091),
                new MSPeak(462.3342867, 1.010024938),
                new MSPeak(464.3499368, 4.88356222),
                new MSPeak(482.3605015, 2.341503307),
                new MSPeak(563.5397726, 1.035560548),
                new MSPeak(565.5554226, 3.403229444),
                new MSPeak(661.5166678, 1.713527115),
                new MSPeak(748.6214672, 8.370506766)
        ));
        double score = scorer.modifiedCosine(spec1, spec2);
        assertEquals(0.9424, score, 1e-4, "Cosine score between spec1 and spec2 should be approximately 0.9424");
    }

    @Test
    void testModifiedCosine() {
        // 1) Build raw spectra
        Spectrum spec1 = new Spectrum(List.of(
                new MSPeak(74.0964258, 0.6203296461),
                new MSPeak(84.08077574, 5.723794737),
                new MSPeak(88.11207587, 15.92527652),
                new MSPeak(182.0576709, 1.229660568),
                new MSPeak(184.073321, 1.357979413),
                new MSPeak(225.2576775, 3.670435487),
                new MSPeak(267.2682422, 7.879446941),
                new MSPeak(283.2995423, 2.132216341),
                new MSPeak(285.2788068, 3.005365091),
                new MSPeak(462.3342867, 1.010024938)

        ));

        Spectrum spec2 = new Spectrum(List.of(
                new MSPeak(88.11207587, 15.92527652),
                new MSPeak(182.0576709, 1.229660568),
                new MSPeak(184.073321, 1.357979413),
                new MSPeak(249.2576775, 1.069886032),
                new MSPeak(267.2682422, 7.879446941),
                new MSPeak(283.2995423, 2.132216341),
                new MSPeak(285.2788068, 3.005365091),
                new MSPeak(462.3342867, 1.010024938),
                new MSPeak(464.3499368, 4.88356222),
                new MSPeak(482.3605015, 2.341503307)
        ));

        double ppmOffset = 12.0;
        List<MSPeak> extra = new ArrayList<>();
        // Tomamos los dos primeros picos de spec1 (o los que quieras)
        for (MSPeak p : spec1.getPeaks().subList(0, 2)) {
            double delta = p.getMz() * ppmOffset / 1e6;
            extra.add(new MSPeak(p.getMz() + delta, p.getIntensity()));
            extra.add(new MSPeak(p.getMz() - delta, p.getIntensity()));
        }
        List<MSPeak> spec2WithExtras = new ArrayList<>(spec2.getPeaks());
        spec2WithExtras.addAll(extra);
        Spectrum spec2Extended = new Spectrum(spec2WithExtras);

        SpectrumScorer scorer = new SpectrumScorer(MzToleranceMode.PPM, 0.0);

        // 4) Prueba para distintas tolerancias
        double[] tolerances = {10.0, 12.0, 15.0};

        for (double tol : tolerances) {
            scorer.setTolValue(tol);          // ajusta la tolerancia en ppm
            System.out.println("Testing modified cosine with tolerance: " + tol);
            Pair<double[], double[]> padded = scorer.padMSPeaks(spec1, spec2Extended);
            double[] vecA = padded.getLeft();
            double[] vecB = padded.getRight();
            double score = scorer.modifiedCosine(spec1, spec2);
        }
    }
@Test
    void testCosine() {
        // 1) Build raw spectra
        Spectrum spec1 = new Spectrum(List.of(
                new MSPeak(84.08077574, 5.723794737),
                new MSPeak(88.11207587, 15.92527652),
                new MSPeak(182.0576709, 1.229660568),
                new MSPeak(184.073321, 1.357979413),
                new MSPeak(225.2576775, 3.670435487),
                new MSPeak(267.2682422, 7.879446941),
                new MSPeak(283.2995423, 2.132216341),
                new MSPeak(285.2788068, 3.005365091),
                new MSPeak(462.3342867, 1.010024938)

        ));

        Spectrum spec2 = new Spectrum(List.of(
                new MSPeak(88.11207587, 15.92527652),
                new MSPeak(182.0576709, 1.229660568),
                new MSPeak(184.073321, 1.357979413),
                new MSPeak(267.2682422, 7.879446941),
                new MSPeak(283.2995423, 2.132216341),
                new MSPeak(285.2788068, 3.005365091),
                new MSPeak(462.3342867, 1.010024938),
                new MSPeak(482.3605015, 2.341503307)
        ));
        double score = scorer.cosineScore(spec1, spec2);
        System.out.println("Cosine score: " + score);
        assertEquals(0.929568, score, 1e-6, "Cosine score between spec1 and spec2 should be approximately 0.9424");
    }

}

