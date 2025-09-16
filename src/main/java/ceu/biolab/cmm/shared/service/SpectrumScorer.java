package ceu.biolab.cmm.shared.service;

import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.msFeature.MSPeak;
import ceu.biolab.cmm.shared.domain.msFeature.ScoreType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Utility class to compute spectral similarity scores between two peak lists.
 */
public class SpectrumScorer {
    private final MzToleranceMode tolMode;
    private final double tolValue;

    public SpectrumScorer(MzToleranceMode tolMode, double tolValue) {
        this.tolMode = tolMode;
        this.tolValue = tolValue;
    }

    public double compute(ScoreType type, List<MSPeak> spec1, List<MSPeak> spec2) {
        return switch (type) {
            case COSINE -> cosineScore(spec1, spec2);
            case MODIFIED_COSINE -> modifiedCosine(spec1, spec2);
        };
    }

    public Pair<double[], double[]> padMSPeaks(List<MSPeak> specA, List<MSPeak> specB) {
        List<MSPeak> a = getNormalizedPeaks(specA);
        List<MSPeak> b = getNormalizedPeaks(specB);

        List<Double> allMz = new ArrayList<>();
        for (MSPeak p : a) {
            allMz.add(p.getMz());
        }
        for (MSPeak p : b) {
            allMz.add(p.getMz());
        }
        Collections.sort(allMz);

        List<Double> centers = new ArrayList<>();
        for (Double mz : allMz) {
            if (centers.isEmpty()) {
                centers.add(mz);
            } else {
                double last = centers.get(centers.size() - 1);
                double tolDa = toleranceDa(last);
                if (Math.abs(mz - last) <= tolDa) {
                    continue;
                }
                centers.add(mz);
            }
        }

        int n = centers.size();
        double[] vecA = new double[n];
        double[] vecB = new double[n];

        for (int i = 0; i < n; i++) {
            double center = centers.get(i);
            double tolDa = toleranceDa(center);
            vecA[i] = sumIntensitiesWithinTol(a, center, tolDa);
            vecB[i] = sumIntensitiesWithinTol(b, center, tolDa);
        }

        return Pair.of(vecA, vecB);
    }

    public double cosineScore(List<MSPeak> specA, List<MSPeak> specB) {
        List<MSPeak> a = getNormalizedPeaks(specA);
        List<MSPeak> b = getNormalizedPeaks(specB);
        a.sort(Comparator.comparingDouble(MSPeak::getMz));
        b.sort(Comparator.comparingDouble(MSPeak::getMz));

        double normA = 0.0;
        double normB = 0.0;
        for (MSPeak p : a) {
            normA += p.getIntensity() * p.getIntensity();
        }
        for (MSPeak p : b) {
            normB += p.getIntensity() * p.getIntensity();
        }
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        int i = 0;
        int j = 0;
        double dot = 0.0;
        while (i < a.size() && j < b.size()) {
            MSPeak pa = a.get(i);
            MSPeak pb = b.get(j);
            double mzA = pa.getMz();
            double mzB = pb.getMz();
            double tolDa = toleranceDa(mzA);
            double diff = mzA - mzB;
            if (Math.abs(diff) <= tolDa) {
                dot += pa.getIntensity() * pb.getIntensity();
                i++;
                j++;
            } else if (diff > 0) {
                j++;
            } else {
                i++;
            }
        }

        return (dot == 0.0) ? 0.0 : (dot / (Math.sqrt(normA) * Math.sqrt(normB)));
    }

    public double modifiedCosine(List<MSPeak> specA, List<MSPeak> specB) {
        return cosineScore(specA, specB);
    }

    public void normalizeIntensities(List<MSPeak> peaks) {
        if (peaks == null || peaks.isEmpty()) {
            return;
        }

        double max = 0.0;
        for (MSPeak peak : peaks) {
            if (peak.getIntensity() > max) {
                max = peak.getIntensity();
            }
        }

        if (max == 0.0) {
            max = 1.0;
        }

        for (MSPeak peak : peaks) {
            peak.setIntensity(peak.getIntensity() / max);
        }
    }

    private double sumIntensitiesWithinTol(List<MSPeak> peaks, double mzRef, double tolDa) {
        double sum = 0.0;
        for (MSPeak p : peaks) {
            if (Math.abs(p.getMz() - mzRef) <= tolDa) {
                sum += p.getIntensity();
            }
        }
        return sum;
    }

    private List<MSPeak> getNormalizedPeaks(List<MSPeak> peaks) {
        if (peaks == null || peaks.isEmpty()) {
            return new ArrayList<>();
        }

        List<MSPeak> sorted = new ArrayList<>(peaks);
        sorted.sort(Comparator.comparingDouble(MSPeak::getMz));
        List<MSPeak> collapsed = new ArrayList<>();
        MSPeak current = null;
        for (MSPeak p : sorted) {
            if (current == null) {
                current = new MSPeak(p.getMz(), p.getIntensity());
            } else {
                double tolDa = toleranceDa(current.getMz());
                if (Math.abs(p.getMz() - current.getMz()) <= tolDa) {
                    current.setIntensity(Math.max(current.getIntensity(), p.getIntensity()));
                } else {
                    collapsed.add(current);
                    current = new MSPeak(p.getMz(), p.getIntensity());
                }
            }
        }
        if (current != null) {
            collapsed.add(current);
        }

        double maxI = 0.0;
        for (MSPeak p : collapsed) {
            maxI = Math.max(maxI, p.getIntensity());
        }
        if (maxI <= 0.0) {
            maxI = 1.0;
        }
        for (MSPeak p : collapsed) {
            p.setIntensity(p.getIntensity() / maxI);
        }
        return collapsed;
    }

    private double toleranceDa(double mzRef) {
        return (tolMode == MzToleranceMode.PPM)
                ? mzRef * tolValue / 1_000_000.0
                : tolValue / 1000.0;
    }
}

