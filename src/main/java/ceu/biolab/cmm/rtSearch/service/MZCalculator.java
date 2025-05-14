package ceu.biolab.cmm.rtSearch.service;

public class MZCalculator {
    public static Double getMZFromSingleChargedMonoMass(Double monoisotopicWeight, Double adductValue) {
        return monoisotopicWeight + adductValue;
    }

    public static Double getMZFromMultiChargedMonoMass(Double monoisotopicWeight, Double adductValue, int charge) {
        double result = monoisotopicWeight;
        result /= charge;
        result += adductValue;
        return result;
    }

    public static Double getMZFromMultimerMonoMass(Double monoisotopicWeight, Double adductValue, int numberMultimers) {
        double result = monoisotopicWeight;
        result *= numberMultimers;
        result += adductValue;
        return result;
    }
}
