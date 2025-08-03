package ceu.biolab.cmm.MSMS.domain;

import ceu.biolab.cmm.shared.domain.msFeature.MSPeak;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Spectrum {
    private List<MSPeak> peaks;

    public Spectrum() {
        this.peaks = new ArrayList<>();
    }

    public Spectrum(List<MSPeak> peaks) {
        this.peaks = peaks;
    }

    /**
     * This method returns a Spectrum with normalized intensities
     */
    public Spectrum normalize() {
        if (peaks.isEmpty()) {
            return new Spectrum(new ArrayList<>());
        }

        // 1. Encontrar la intensidad máxima
        double maxIntensity = 0.0;
        for (MSPeak peak : peaks) {
            if (peak.getIntensity() > maxIntensity) {
                maxIntensity = peak.getIntensity();
            }
        }

        // Evitar división por cero
        if (maxIntensity == 0.0) {
            maxIntensity = 1.0;
        }

        // 2. Crear nueva lista de picos normalizados
        List<MSPeak> normalizedPeaks = new ArrayList<>();
        for (MSPeak peak : peaks) {
            double normalizedIntensity = (peak.getIntensity() / maxIntensity) * 100.0;
            normalizedPeaks.add(new MSPeak(peak.getMz(), normalizedIntensity));
        }

        // 3. Devolver nuevo objeto Spectrum
        return new Spectrum(normalizedPeaks);
    }

    public static double[] cosineZeroFilled(Spectrum a, Spectrum b, double tolerance) {
        List<Double> intensitiesA = new ArrayList<>();
        List<Double> intensitiesB = new ArrayList<>();

        for (MSPeak peakA : a.getPeaks()) {
            double matched = findIntensityAtMz(b, peakA.getMz(), tolerance);
            intensitiesA.add(peakA.getIntensity());
            intensitiesB.add(matched);
        }

        for (MSPeak peakB : b.getPeaks()) {
            if (findIntensityAtMz(a, peakB.getMz(), tolerance) == 0.0) {
                intensitiesA.add(0.0);
                intensitiesB.add(peakB.getIntensity());
            }
        }

        return cosineScore(toArray(intensitiesA), toArray(intensitiesB));
    }

    private static double findIntensityAtMz(Spectrum spectrum, double mz, double tolerance) {
        for (MSPeak p : spectrum.getPeaks()) {
            if (Math.abs(p.getMz() - mz) <= tolerance) {
                return p.getIntensity();
            }
        }
        return 0.0;
    }

    private static double[] toArray(List<Double> list) {
        return list.stream().mapToDouble(Double::doubleValue).toArray();
    }

    public static double[] cosineScore(double[] a, double[] b) {
        if (a.length != b.length) throw new IllegalArgumentException("Arrays must have same length");

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) {
            return new double[]{};
        }

        return new double[]{dot / (Math.sqrt(normA) * Math.sqrt(normB))};
    }
}
