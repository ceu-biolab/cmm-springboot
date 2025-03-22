package com.example.myapp.model.adduct;

import ceu.biolab.*;
import com.example.myapp.model.IonizationMode;
import com.example.myapp.repository.CompoundRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

import java.util.Map;

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
                return getMonoMassFromSingleChargedMZ(mz, adductValue);
            }

            if (multimer > 1) { // Dimer or Trimer with a charge of +/- 2 or +/- 3
                return getMonoMassFromMultimerMZ(mz, adductValue, multimer);
            } else { // Monomer with a specified charge of +/- 2 or +/- 3
                return getMonoMassFromMultiChargedMZ(mz, adductValue, charge);
            }
        } catch (IncorrectAdduct e) {
            throw new RuntimeException(e);
        }
    }

    public static Double getMonoMassFromSingleChargedMZ(Double experimentalMass, Double adductValue) {
        return experimentalMass - adductValue;
    }

    private static Double getMonoMassFromMultiChargedMZ(double experimentalMass, double adductValue, int charge) {
        double result = experimentalMass;
        result -= adductValue;
        result *= charge;
        return result;
    }

    private static Double getMonoMassFromMultimerMZ(double experimentalMass, double adductValue, int numberAtoms) {
        double result = experimentalMass;
        result -= adductValue;
        result /= numberAtoms;
        return result;
    }

    public static Double getMZFromSingleChargedMonoMass(Double monoisotopicWeight, Double adductValue) {
        return monoisotopicWeight + adductValue;
    }

    private static Double getMZFromMultiChargedMonoMass(double monoisotopicWeight, double adductValue, int charge) {
        double result = monoisotopicWeight;
        result /= charge;
        result += adductValue;
        return result;
    }

    private static Double getMZFromMultimerMonoMass(double monoisotopicWeight, double adductValue, int numberMultimers) {
        double result = monoisotopicWeight;
        result *= numberMultimers;
        result += adductValue;
        return result;
    }

    /**
     * Calculate the adduct Mass based on the monoisotopic weight
     *
     * @param monoisotopicWeight Experimental mass of the compound
     * @param adduct              adduct name (M+H, 2M+H, M+2H, etc..)
     * @return the mass difference within the tolerance respecting to the massToSearch
     */
    public static Double getMassOfAdductFromMonoMass(Double monoisotopicWeight, String adduct, IonizationMode ionizationMode) {
        Map<String, String> adductMap = AdductProcessing.getAdduct(adduct, ionizationMode);
        Map.Entry<String, String> entry = adductMap.entrySet().iterator().next();
        String adductFromMap = adductMap.keySet().iterator().next();

        Adduct adductObj;

        try {
            adductObj = new Adduct(adductFromMap);

            double adductValue = adductObj.getAdductMass();

            int charge = adductObj.getAdductCharge();
            int multimer = adductObj.getMultimer();

            if (charge == 1 && multimer == 1) { // Default case: Monomer with Charge +/- 1
                return getMZFromSingleChargedMonoMass(monoisotopicWeight, adductValue);
            }

            if (multimer > 1) { // Dimer or Trimer with a charge of +/- 2 or +/- 3
                return getMZFromMultimerMonoMass(monoisotopicWeight, adductValue, multimer);
            } else { // Monomer with a specified charge of +/- 2 or +/- 3
                return getMZFromMultiChargedMonoMass(monoisotopicWeight, adductValue, charge);
            }
        } catch (IncorrectAdduct | NotFoundElement | IncorrectFormula e) {
            throw new RuntimeException(e);
        }
    }
}
