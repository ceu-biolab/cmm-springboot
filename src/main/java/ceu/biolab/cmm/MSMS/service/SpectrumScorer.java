package ceu.biolab.cmm.MSMS.service;

import ceu.biolab.cmm.MSMS.domain.Peak;
import ceu.biolab.cmm.MSMS.domain.ScoreType;
import ceu.biolab.cmm.MSMS.domain.Spectrum;
import ceu.biolab.cmm.MSMS.domain.ToleranceMode;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SpectrumScorer {


    private final ToleranceMode tolMode;
    private final double tolValue;
    private final double referenceMz;

    public SpectrumScorer(ToleranceMode tolMode, double tolValue, double referenceMz) {
        this.tolMode = tolMode;
        this.tolValue = tolValue;
        this.referenceMz = referenceMz;
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
    // TODO repasar pq las listas no cogen toddas las intentsidades

    public Pair<double[], double[]> padPeaks(Spectrum specA, Spectrum specB) {
        normalizeIntensities(specA);
        normalizeIntensities(specB);

        double[]normalizedA = specA.getPeaks().stream()
                .mapToDouble(Peak::getIntensity)
                .toArray();
        double[]normalizedB = specB.getPeaks().stream()
                .mapToDouble(Peak::getIntensity)
                .toArray();
        System.out.println("Normalized A: " + Arrays.toString(normalizedA));
        System.out.println("Normalized B: " + Arrays.toString(normalizedB));
        List<Double> mzOrder = new ArrayList<>();
        Map<Double, Double> mapA = new LinkedHashMap<>();
        Map<Double, Double> mapB = new HashMap<>();

        // 1) Recorre specA en su orden y anota m/z + intensidad
        for (Peak p : specA.getPeaks()) {
            double mz = p.getMz();
            if (!mzOrder.contains(mz)) {
                mzOrder.add(mz);
                mapA.put(mz, p.getIntensity());
            } else {
                mapA.put(mz, mapA.get(mz) + p.getIntensity());
            }
        }

        // 2) Mapea specB pero solo guarda en mapB;  lo usaremos luego
        for (Peak p : specB.getPeaks()) {
            double mz = p.getMz();
            // Si B tiene duplicados en misma m/z, sumamos intensidades
            mapB.merge(mz, p.getIntensity(), Double::sum);
        }

        // 3) Añade al final de mzOrder los m/z de B que no estaban en A
        for (Double mz : mapB.keySet()) {
            if (!mapA.containsKey(mz)) {
                mzOrder.add(mz);
            }
        }

        // 4) Construye los vectores en el orden de mzOrder
        int n = mzOrder.size();
        double[] vecA = new double[n];
        double[] vecB = new double[n];
        for (int i = 0; i < n; i++) {
            double mz = mzOrder.get(i);
            vecA[i] = mapA.getOrDefault(mz, 0.0);
            vecB[i] = mapB.getOrDefault(mz, 0.0);
        }

        return Pair.of(vecA, vecB);
    }

    public double obtainbinWidth() {
        if(this.tolMode==ToleranceMode.PPM) {
            return (tolValue / 1_000_000.0) * referenceMz;
        } else {
            return tolValue;
        }
    }
    public double obtainMaxValue(Spectrum spec) {
        return spec.getPeaks().stream()
                .mapToDouble(Peak::getMz)
                .max()
                .orElse(1.0);
    }
    public double obtainMinValue(Spectrum spec) {
        return spec.getPeaks().stream()
                .mapToDouble(Peak::getMz)
                .min()
                .orElse(0.0);
    }
    public  double cosineScore(Spectrum specA, Spectrum specB) {

        Pair<double[], double[]> padded = padPeaks(specA, specB);
        double[] vecA = padded.getLeft();
        double[] vecB = padded.getRight();
        System.out.println("Vector A: " + Arrays.toString(vecA));
        System.out.println("Vector B: " + Arrays.toString(vecB));
        double dot = 0.0, normA = 0.0, normB = 0.0;
        if (vecA.length != vecB.length) {
            throw new IllegalArgumentException("Vectors must be of the same length");
        }
        int n = vecA.length;
        for (int i = 0; i < n; i++) {
            dot   += vecA[i] * vecB[i];
            normA += vecA[i] * vecA[i];
            normB += vecB[i] * vecB[i];
        }
        if (normA == 0.0 || normB == 0.0) {return 0.0;}
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
    public  double modifiedCosine(Spectrum specA, Spectrum specB) {

        normalizeIntensities(specA);
        normalizeIntensities(specB);


        // 2) Hacer el padding manteniendo orden de A primero, luego B-only
        Pair<double[], double[]> padded = padPeaks(specA, specB);
        double[] vecA = padded.getLeft();
        double[] vecB = padded.getRight();

        // 3) Reconstruir la lista de m/z en el mismo orden
        List<Double> mzOrder = new ArrayList<>();
        // primero los de A
        for (Peak p : specA.getPeaks()) {
            if (!mzOrder.contains(p.getMz())) mzOrder.add(p.getMz());
        }
        // luego los de B que no estaban en A
        for (Peak p : specB.getPeaks()) {
            if (!mzOrder.contains(p.getMz())) mzOrder.add(p.getMz());
        }

        // 4) Greedy matching sobre los vectores padded
        boolean[] usedB = new boolean[mzOrder.size()];
        double dot = 0, normA = 0, normB = 0;

        for (int i = 0; i < mzOrder.size(); i++) {
            double aI = vecA[i];
            double aMz = mzOrder.get(i);
            if (aI <= 0) continue;
            // buscar primer j no usado cuya diferencia de m/z esté dentro de tolerancia
            for (int j = 0; j < mzOrder.size(); j++) {
                if (usedB[j]) continue;
                double bMz = mzOrder.get(j);
                if (Math.abs(aMz - bMz) <= tolValue) {
                    double bI = vecB[j];
                    if (bI > 0) {
                        usedB[j] = true;
                        dot   += aI * bI;
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
    public void normalizeIntensities(Spectrum spec) {
        double max = spec.getPeaks().stream()
                .mapToDouble(Peak::getIntensity)
                .max()
                .orElse(1.0);
        for (Peak p : spec.getPeaks()) {
            p.setIntensity(p.getIntensity() / max);
        }
    }
}
