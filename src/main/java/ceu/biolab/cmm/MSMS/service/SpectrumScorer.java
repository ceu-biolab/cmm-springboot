package ceu.biolab.cmm.MSMS.service;

import ceu.biolab.cmm.MSMS.domain.ScoreType;
import ceu.biolab.cmm.MSMS.domain.Spectrum;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.msFeature.MSPeak;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static java.lang.Math.max;

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
        normalizeIntensities(specA);
        normalizeIntensities(specB);

        List<Double> mzOrder = new ArrayList<>();
        Map<Double, Double> mapA = new LinkedHashMap<>();
        Map<Double, Double> mapB = new HashMap<>();

        // 1) Recorre specA en su orden y anota m/z + intensidad
        for (MSPeak peak : specA.getPeaks()) {
            double mz = peak.getMz();
            if (!mzOrder.contains(mz)) {
                mzOrder.add(mz);
                mapA.put(mz, peak.getIntensity());
            } else {
                mapA.put(mz, max(mapA.get(mz), peak.getIntensity()));
            }
        }

        // 2) Mapea specB pero solo guarda en mapB;  lo usaremos luego
        for (MSPeak p : specB.getPeaks()) {
            double mz = p.getMz();
            // Si B tiene duplicados en misma m/z, sumamos intensidades
            mapB.merge(mz, p.getIntensity(), Double::sum);
        }

        // todo revisar si esto es correcto, porque no se está teniendo en cuenta la tolerancia
        // 3) Añade al final de mzOrder los m/z de B que no estaban en A
        for (Double mzB : mapB.keySet()) {
            boolean matched = false;
            for (Double mzA : mzOrder) {
                double delta = Math.abs(mzA - mzB);
                double tolDa = (tolMode == MzToleranceMode.PPM)
                        ? mzA * tolValue / 1_000_000.0
                        : tolValue;
                if (delta <= tolDa) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                mzOrder.add(mzB);
            }
        }

        int n = mzOrder.size();
        double[] vecA = new double[n];
        double[] vecB = new double[n];

        for (int i = 0; i < n; i++) {
            double mzRef = mzOrder.get(i);
            // calculamos la tolerancia en Da para este bin
            double tolDa = (tolMode == MzToleranceMode.PPM)
                    ? mzRef * tolValue / 1_000_000.0
                    : tolValue;

            // sumamos todas las intensidades que entren en esa ventana
            vecA[i] = sumIntensitiesWithinTol(specA, mzRef, tolDa);
            vecB[i] = sumIntensitiesWithinTol(specB, mzRef, tolDa);
        }

        System.out.println("Mz Order: " + Arrays.toString(mzOrder.toArray()));
        System.out.println("Vector A: " + Arrays.toString(vecA));
        System.out.println("Vector B: " + Arrays.toString(vecB));
        return Pair.of(vecA, vecB);
    }

    public  double cosineScore(Spectrum specA, Spectrum specB) {
        Pair<double[], double[]> padded = padMSPeaks(specA, specB);
        return cosine(padded.getLeft(), padded.getRight());
    }

    public  double modifiedCosine(Spectrum specA, Spectrum specB) {
        normalizeIntensities(specA);
        normalizeIntensities(specB);

        // 2) Hacer el padding manteniendo orden de A primero, luego B-only
        Pair<double[], double[]> padded = padMSPeaks(specA, specB);
        double[] vecA = padded.getLeft();
        double[] vecB = padded.getRight();

        //3) Reconstruir la lista de m/z en el mismo orden
        List<Double> mzOrder = new ArrayList<>();
        boolean[] usedB = new boolean[mzOrder.size()];

        double dot = 0, normA = 0, normB = 0;

        for (int i = 0; i < mzOrder.size(); i++) {
            double aI = vecA[i];
            if (aI <= 0) continue;

            double aMz = mzOrder.get(i);

            // busca el primer pico de B dentro de tolerancia
            for (int j = 0; j < mzOrder.size(); j++) {
                if (usedB[j]) continue;

                double bMz = mzOrder.get(j);
                if (Math.abs(aMz - bMz) <= tolValue) {
                    double bI = vecB[j];
                    if (bI > 0) {
                        usedB[j] = true;
                        dot += aI * bI;
                        normA += aI * aI;
                        normB += bI * bI;
                    }
                    break;
                }
            }
        }

        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
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

}
