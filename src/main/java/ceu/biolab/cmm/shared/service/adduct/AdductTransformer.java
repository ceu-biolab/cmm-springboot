package ceu.biolab.cmm.shared.service.adduct;

import ceu.biolab.Adduct;
import ceu.biolab.IncorrectAdduct;
import ceu.biolab.IncorrectFormula;
import ceu.biolab.NotFoundElement;
import ceu.biolab.cmm.rtSearch.repository.CompoundRepository;

import ceu.biolab.cmm.shared.domain.Constants;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.adduct.AdductList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdductTransformer {
    private static final Logger logger = LoggerFactory.getLogger(CompoundRepository.class);

    /**
     * Returns the ppm difference between measured mass and theoretical mass
     * @param measuredMass    Mass measured by MS
     * @param theoreticalMass Theoretical mass of the compound
     */
    public static int calculatePPMIncrement(Double measuredMass, Double theoreticalMass) {
        int ppmIncrement;
        ppmIncrement = (int) Math.round(Math.abs((measuredMass - theoreticalMass) * 1000000
                / theoreticalMass));
        return ppmIncrement;
    }

    /**
     * Set the relative percentage difference between measured value and
     * theoretical value
     *
     * @param experimentalRMT RMT in CEMS experiment
     * @param theoreticalRMT  RMT in CEMS experiment
     * @return
     */
    public static Integer calculatePercentageError(Double experimentalRMT, Double theoreticalRMT) {
        int RMTError;
        RMTError = (int) Math.round(Math.abs((experimentalRMT - theoreticalRMT) / theoreticalRMT * 100));
        return RMTError;
    }

    /**
     * Calculate the mass to search depending on the adduct hypothesis
     *
     * @param mz Experimental mass of the compound
     * @param adduct           adduct name (M+H, 2M+H, M+2H, etc.)
     * @return the mass difference within the tolerance respecting to the
     * massToSearch
     */
    public static Double getMonoisotopicMassFromMZ(Double mz, String adduct, IonizationMode ionizationMode) {
        Adduct adductObj = null;
        try {
            adductObj = AdductProcessing.getAdductFromString(adduct, ionizationMode, mz);
            double adductValue = adductObj.getAdductMass();
            int charge = adductObj.getAdductCharge();
            int multimer = adductObj.getMultimer();
            logger.info("adduct value: {}", adductValue);
            logger.info("Multimer: {}", multimer);
            logger.info("charge: {}", charge);

            if (charge == 1 && multimer == 1) { // Default case: Monomer with Charge +/- 1
                return getMonoMassFromSingleChargedMZ(mz, adductValue, charge);
            }

            if (multimer > 1) { // Dimer or Trimer with a charge of +/- 2 or +/- 3
                return getMonoMassFromMultimerMZ(mz, adductValue, charge, multimer);
            } else { // Monomer with a specified charge of +/- 2 or +/- 3
                return getMonoMassFromMultiChargedMZ(mz, adductValue, charge);
            }
        } catch (IncorrectAdduct e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * This method calculates the monoisotopic mass from a multi charged experimental mass (m/z)
     * @param experimentalMass the experimental mass as a double
     * @param adductValue the mass of the adduct as a double
     * @return the monoisotopic weight as a double
     */
    public static Double getMonoMassFromSingleChargedMZ(Double experimentalMass, Double adductValue, int charge) {
        return experimentalMass - adductValue + charge * Constants.ELECTRON_MONOISOTOPIC_MASS;
    }

    /**
     * This method calculates the monoisotopic mass from a multi charged experimental mass (m/z)
     * @param experimentalMass the experimental mass as a double
     * @param adductValue the mass of the adduct as a double
     * @param charge the number of charges of the adduct as an integer
     * @return the monoisotopic weight as a double
     */
    private static Double getMonoMassFromMultiChargedMZ(double experimentalMass, double adductValue, int charge) {
        double result = experimentalMass;
        result -= adductValue;
        result *= charge;
        result = result + charge * Constants.ELECTRON_MONOISOTOPIC_MASS;
        return result;
    }

    /**
     * This method calculates the monoisotopic mass from the experimental mass (m/z)
     * @param experimentalMass the experimental mass as a double
     * @param adductValue the mass of the adduct as a double
     * @param charge the number of charges of the adduct as an integer
     * @param numberAtoms the number of atoms as an integer
     * @return the monoisotopic weight as a double
     */
    private static Double getMonoMassFromMultimerMZ(double experimentalMass, double adductValue, int charge, int numberAtoms) {
        double result = experimentalMass;
        result -= adductValue;
        result /= numberAtoms;
        result = result + charge * Constants.ELECTRON_MONOISOTOPIC_MASS;
        return result;
    }

    /**
     * This method calculates the experimental mass based on a single charged monoisotopic weight
     * @param monoisotopicWeight monoisotopic weight as Double
     * @param adductValue the mass of the adduct as a double
     * @param charge the number of charges of the adduct as an integer
     * @return the experimental mass as a double
     */
    public static Double getMZFromSingleChargedMonoMass(Double monoisotopicWeight, Double adductValue, int charge) {
        return monoisotopicWeight + adductValue - charge * Constants.ELECTRON_MONOISOTOPIC_MASS;
    }

    /**
     * This method calculates the experimental mass based on a multi charged monoisotopic weight
     * @param monoisotopicWeight monoisotopic weight as Double
     * @param adductValue the mass of the adduct as a double
     * @param charge the number of charges of the adduct as an integer
     * @return the experimental mass as a double
     */
    private static Double getMZFromMultiChargedMonoMass(double monoisotopicWeight, double adductValue, int charge) {
        double result = monoisotopicWeight;
        result /= charge;
        result += adductValue;
        result = result - charge * Constants.ELECTRON_MONOISOTOPIC_MASS;
        return result;
    }

    /**
     * This method calculates the MZ based on the monoisotopic weight
     * @param monoisotopicWeight monoisotopic weight as Double
     * @param adductValue the mass of the adduct as a double
     * @param charge the number of charges of the adduct as an integer
     * @param numberMultimers the number of multimers of the adduct as an integer
     * @return the experimental mass as a double
     */
    private static Double getMZFromMultimerMonoMass(double monoisotopicWeight, double adductValue, int charge, int numberMultimers) {
        double result = monoisotopicWeight;
        result *= numberMultimers;
        result += adductValue;
        result = result - charge * Constants.ELECTRON_MONOISOTOPIC_MASS;
        return result;
    }

    /**
     * Calculate the adduct Mass based on the monoisotopic weight
     *
     * @param monoisotopicWeight Experimental mass of the compound
     * @param adduct              adduct name (M+H, 2M+H, M+2H, etc..)
     * @param ionizationMode  ionization mode: positive or negative
     * @return the mass difference within the tolerance respecting to the massToSearch
     */
    public static Double getMassOfAdductFromMonoMass(Double monoisotopicWeight, String adduct, IonizationMode ionizationMode) {
        Adduct adductObj;

        try {
            adductObj = new Adduct(adduct);
            double adductValue = adductObj.getAdductMass();

            int charge = adductObj.getAdductCharge();
            int multimer = adductObj.getMultimer();

            if (charge == 1 && multimer == 1) { // Default case: Monomer with Charge +/- 1
                return getMZFromSingleChargedMonoMass(monoisotopicWeight, adductValue, charge);
            }

            if (multimer > 1) { // Dimer or Trimer with a charge of +/- 2 or +/- 3
                return getMZFromMultimerMonoMass(monoisotopicWeight, adductValue, charge, multimer);
            } else { // Monomer with a specified charge of +/- 2 or +/- 3
                return getMZFromMultiChargedMonoMass(monoisotopicWeight, adductValue, charge);
            }
        } catch (IncorrectAdduct | NotFoundElement | IncorrectFormula e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * This method gets the number of charges of the Adduct as a String
     * @param adduct Adduct as a String
     * @return number of charges as an Integer
     */
    public static int getChargeOfAdduct(String adduct) {
        if (AdductList.CHARGE_3.contains(adduct)) {
            return 3;
        } else if (AdductList.CHARGE_2.contains(adduct)) {
            return 2;
        } else {
            return 1; //default: 1 charge
        }
    }

}
