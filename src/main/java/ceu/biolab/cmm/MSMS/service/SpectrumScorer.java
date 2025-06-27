package ceu.biolab.cmm.MSMS.service;

import ceu.biolab.cmm.MSMS.domain.Peak;
import ceu.biolab.cmm.MSMS.domain.ScoreType;
import ceu.biolab.cmm.MSMS.domain.Spectrum;
import ceu.biolab.cmm.MSMS.domain.ToleranceMode;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.max;

public class SpectrumScorer {


    private  ToleranceMode tolMode;
    private double tolValue;


    public SpectrumScorer(ToleranceMode tolMode, double tolValue) {
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

    public Pair<double[], double[]> padPeaks(Spectrum specA, Spectrum specB) {
        normalizeIntensities(specA);
        normalizeIntensities(specB);

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
                mapA.put(mz, max(mapA.get(mz), p.getIntensity()));
            }
        }

        // 2) Mapea specB pero solo guarda en mapB;  lo usaremos luego
        for (Peak p : specB.getPeaks()) {
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
                double tolDa = (tolMode == ToleranceMode.PPM)
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
            double tolDa = (tolMode == ToleranceMode.PPM)
                    ? mzRef * tolValue / 1_000_000.0
                    : tolValue;

            // sumamos todas las intensidades de A que entren en esa ventana
            double sumA = 0.0;
            for (Map.Entry<Double, Double> e : mapA.entrySet()) {
                if (Math.abs(e.getKey() - mzRef) <= tolDa) {
                    sumA += e.getValue();
                }
            }
            vecA[i] = sumA;

            // idéntico para B
            double sumB = 0.0;
            for (Map.Entry<Double, Double> e : mapB.entrySet()) {
                if (Math.abs(e.getKey() - mzRef) <= tolDa) {
                    sumB += e.getValue();
                }
            }
            vecB[i] = sumB;
        }
        System.out.println("Mz Order: " + Arrays.toString(mzOrder.toArray()));
        System.out.println("Vector A: " + Arrays.toString(vecA));
        System.out.println("Vector B: " + Arrays.toString(vecB));
        return Pair.of(vecA, vecB);
    }
    public  double cosineScore(Spectrum specA, Spectrum specB) {

        Pair<double[], double[]> padded = padPeaks(specA, specB);
        double[] vecA = padded.getLeft();
        double[] vecB = padded.getRight();
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

        //3) Reconstruir la lista de m/z en el mismo orden
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
                .max().orElseThrow();
        for (Peak p : spec.getPeaks()) {
            p.setIntensity(p.getIntensity() / max);
        }
    }

    public ToleranceMode getTolMode() {
        return tolMode;
    }

    public double getTolValue() {
        return tolValue;
    }

    public void setTolMode(ToleranceMode tolMode) {
        this.tolMode = tolMode;
    }
    public void setTolValue(double tolValue) {
        this.tolValue = tolValue;
    }
}
