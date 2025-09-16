package ceu.biolab.cmm.MSMSSearch.service;

import ceu.biolab.cmm.MSMSSearch.domain.ScoreType;
import ceu.biolab.cmm.MSMSSearch.domain.Spectrum;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.msFeature.MSPeak;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

@Data
public class SpectrumScorer {
    private MzToleranceMode tolMode;
    private double tolValue;

    public SpectrumScorer(MzToleranceMode tolMode, double tolValue) {
        this.tolMode = tolMode;
        this.tolValue = tolValue;
    }

    public double compute(ScoreType type, Spectrum spec1, Spectrum spec2)  {
        switch (type) {
            case COSINE:
                return cosineScore(spec1, spec2);
            case MODIFIED_COSINE:
                return modifiedCosine(spec1,  spec2);
            default:
                throw new IllegalArgumentException("Unknown score type");
        }
    }

    public Pair<double[], double[]> padMSPeaks(Spectrum specA, Spectrum specB) {
        // Build normalized copies of the peaks so we do not mutate inputs
        List<MSPeak> a = getNormalizedPeaks(specA);
        List<MSPeak> b = getNormalizedPeaks(specB);

        // Collect all m/z values from collapsed, normalized peaks and sort
        List<Double> allMz = new ArrayList<>();
        for (MSPeak p : a) allMz.add(p.getMz());
        for (MSPeak p : b) allMz.add(p.getMz());
        Collections.sort(allMz);

        // Merge into non-overlapping bins using tolerance
        List<Double> centers = new ArrayList<>();
        for (Double mz : allMz) {
            if (centers.isEmpty()) {
                centers.add(mz);
            } else {
                double last = centers.get(centers.size() - 1);
                double tolDa = toleranceDa(last);
                if (Math.abs(mz - last) <= tolDa) {
                    // keep center as the first encountered value to maintain stable order
                    continue;
                } else {
                    centers.add(mz);
                }
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

    public  double cosineScore(Spectrum specA, Spectrum specB) {
        // Greedy one-to-one matching within tolerance using normalized intensities
        List<MSPeak> a = getNormalizedPeaks(specA);
        List<MSPeak> b = getNormalizedPeaks(specB);
        a.sort(Comparator.comparingDouble(MSPeak::getMz));
        b.sort(Comparator.comparingDouble(MSPeak::getMz));

        double normA = 0.0, normB = 0.0;
        for (MSPeak p : a) normA += p.getIntensity() * p.getIntensity();
        for (MSPeak p : b) normB += p.getIntensity() * p.getIntensity();
        if (normA == 0.0 || normB == 0.0) return 0.0;

        int i = 0, j = 0;
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
                i++; j++;
            } else if (diff > 0) {
                j++;
            } else {
                i++;
            }
        }

        return (dot == 0.0) ? 0.0 : (dot / (Math.sqrt(normA) * Math.sqrt(normB)));
    }

    public  double modifiedCosine(Spectrum specA, Spectrum specB) {
        // Basic variant without precursor shift: same greedy matching as cosine.
        // Keeping separate method for future enhancements (e.g., precursor-shift alignment).
        return cosineScore(specA, specB);
    }


    /**
     * This method normalizes the intensities of a spectrum
     * @param fragment the fragment to be normalized
     */
    public void normalizeIntensities(Spectrum fragment) {
        if (fragment.getPeaks().isEmpty()) {
            return;
        }

        // 1. Find max intensity
        double max = 0.0;
        for (MSPeak p : fragment.getPeaks()) {
            if (p.getIntensity() > max) {
                max = p.getIntensity();
            }
        }

        // Avoid division by zero
        if (max == 0.0) {
            max = 1.0;
        }

        // Normalized peaks : scaled to [0, 1]
        for (MSPeak normalizedPeaks : fragment.getPeaks()) {
            normalizedPeaks.setIntensity(normalizedPeaks.getIntensity() / max);
        }
    }

    private static double cosine(double[] a, double[] b) {
        if (a.length != b.length) throw new IllegalArgumentException("Vector length mismatch");

        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * This method sums the intensities found for the same m/z
     * @param spec the fragment which contains the m/z and intensities
     * @param mzRef the m/z as a double
     * @param tolDa the tolerance calculated in Da
     * @return the sum of intensities
     */
    private double sumIntensitiesWithinTol(Spectrum spec, double mzRef, double tolDa) {
        double sum = 0.0;
        for (MSPeak p : spec.getPeaks()) {
            if (Math.abs(p.getMz() - mzRef) <= tolDa) {
                sum += p.getIntensity();
            }
        }
        return sum;
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

    private List<MSPeak> getNormalizedPeaks(Spectrum fragment) {
        if (fragment == null || fragment.getPeaks().isEmpty()) return new ArrayList<>();
        // Collapse close/duplicate peaks within tolerance using max intensity, keep first m/z
        List<MSPeak> peaks = new ArrayList<>(fragment.getPeaks());
        peaks.sort(Comparator.comparingDouble(MSPeak::getMz));
        List<MSPeak> collapsed = new ArrayList<>();
        MSPeak current = null;
        for (MSPeak p : peaks) {
            if (current == null) {
                current = new MSPeak(p.getMz(), p.getIntensity());
            } else {
                double tolDa = toleranceDa(current.getMz());
                if (Math.abs(p.getMz() - current.getMz()) <= tolDa) {
                    // merge by max intensity, keep center m/z
                    current.setIntensity(Math.max(current.getIntensity(), p.getIntensity()));
                } else {
                    collapsed.add(current);
                    current = new MSPeak(p.getMz(), p.getIntensity());
                }
            }
        }
        if (current != null) collapsed.add(current);

        // Normalize to unit max
        double maxI = 0.0;
        for (MSPeak p : collapsed) maxI = Math.max(maxI, p.getIntensity());
        if (maxI <= 0.0) maxI = 1.0;
        for (MSPeak p : collapsed) p.setIntensity(p.getIntensity() / maxI);
        return collapsed;
    }

    private double toleranceDa(double mzRef) {
        return (tolMode == MzToleranceMode.PPM)
                ? mzRef * tolValue / 1_000_000.0
                : tolValue / 1000.0; // MDA to Da
    }

}
