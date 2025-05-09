package ceu.biolab.cmm.msSearch.mzcalculator;

public class MZCalculator {

    /**
     * This method obtains the m/z from a single charged compound from its monoisotopic mass and the adduct mass
     * @param monoisotopicWeight the monoisotopic mass of the compound as a double
     * @param adductValue the mass of the adduct as a double
     * @return the m/z as a double
     */
    public static Double getMZFromSingleChargedMonoMass(Double monoisotopicWeight, Double adductValue) {
        return monoisotopicWeight + adductValue;
    }

    /**
     * This method obtains the m/z from a charged compound from its monoisotopic mass and the adduct mass
     * @param monoisotopicWeight the monoisotopic mass of the compound as a double
     * @param adductValue the mass of the adduct as a double
     * @param charge the number of charges of the adduct as an int
     * @return the m/z as a double
     */
    public static Double getMZFromMultiChargedMonoMass(Double monoisotopicWeight, Double adductValue, int charge) {
        double result = monoisotopicWeight;
        result /= charge;
        result += adductValue;
        return result;
    }

    /**
     *
     * @param monoisotopicWeight the monoisotopic mass of the compound as a double
     * @param adductValue the mass of the adduct as a double
     * @param numberMultimers the number of multimers (dimer, multimer,...) as an int
     * @return the m/z as a double
     */
    public static Double getMZFromMultimerMonoMass(Double monoisotopicWeight, Double adductValue, int numberMultimers) {
        double result = monoisotopicWeight;
        result *= numberMultimers;
        result += adductValue;
        return result;
    }
}
