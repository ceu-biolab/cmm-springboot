package ceu.biolab.cmm.shared.service.adduct;

import ceu.biolab.Adduct;
import ceu.biolab.IncorrectAdduct;
import ceu.biolab.IncorrectFormula;
import ceu.biolab.NotFoundElement;
import ceu.biolab.cmm.msSearch.repository.CompoundRepository;
import ceu.biolab.cmm.shared.domain.Constants;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.adduct.AdductList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdductProcessing {
    private static final Logger logger = LoggerFactory.getLogger(CompoundRepository.class);

    public static int getChargeOfAdduct(String adductName, IonizationMode ionMode) {
        if (adductName == null || adductName.isEmpty()) return 1;

        if (AdductList.CHARGE_2.contains(adductName)) {
            return 2;
        }

        if (AdductList.CHARGE_3 != null && AdductList.CHARGE_3.contains(adductName)) {
            return 3;
        }

        // Default: assume charge 1 if in map, otherwise return 1 as fallback
        if (ionMode == IonizationMode.POSITIVE && AdductList.MAPMZPOSITIVEADDUCTS.containsKey(adductName)) {
            return 1;
        }

        if (ionMode == IonizationMode.NEGATIVE && AdductList.MAPMZNEGATIVEADDUCTS.containsKey(adductName)) {
            return 1;
        }

        return 1;
    }


    public static Adduct getAdductFromString(String adductString, IonizationMode ionizationMode, Double mz) throws IncorrectAdduct {
        try {
            if (ionizationMode == IonizationMode.POSITIVE) {
                if (AdductList.MAPMZPOSITIVEADDUCTS.containsKey(adductString)) {
                    int charge = AdductTransformer.getChargeOfAdduct(adductString);
                    String adductFormula = "[" + adductString + "]" + charge + "+";
                    Adduct adj = new Adduct((adductFormula));
                    logger.info("adduct from processing: {}", adj);
                    return adj;
                } else
                    logger.info("adducts received: {}", adductString);
                    throw new IllegalArgumentException("Adduct not found K: " + adductString);
            } else if (AdductList.MAPMZNEGATIVEADDUCTS.containsKey(adductString)) {
                int charge = AdductTransformer.getChargeOfAdduct(adductString);
                String adductFormula = "[" + adductString + "]" + charge + "-";
                logger.info("adduct FORMULA from processing: {}", adductFormula);
                return new Adduct(adductFormula);
            } else {
                throw new IllegalArgumentException("Adduct not found: " + adductString);
            }
        } catch (NotFoundElement | IncorrectFormula | IncorrectAdduct e) {
            throw new IncorrectAdduct("Invalid adduct: " + adductString + e.getMessage());
        }
    }


    public static String formatAdductString(String adduct, IonizationMode ionizationMode){
        String formattedDetectedAdduct = null;
        int charge = AdductProcessing.getChargeOfAdduct(adduct, ionizationMode);
        formattedDetectedAdduct = "[" + adduct + "]";
        if (charge > 1) {
            formattedDetectedAdduct += charge;
        }
        if (ionizationMode == IonizationMode.POSITIVE) {
            formattedDetectedAdduct += "+";
        } else if (ionizationMode == IonizationMode.NEGATIVE) {
            formattedDetectedAdduct += "-";
        }
        return formattedDetectedAdduct;
    }



    public static String detectAdductBasedOnCompositeSpectrum(IonizationMode ionizationMode, Double mz,
                                                              Set<String> adducts, Map<Double, Double> groupedPeaks) {
        if (groupedPeaks.isEmpty()) {
            return "";
        }

        Map<Double, Double> groupedPeaksFiltered = filterIsotopes(groupedPeaks);
        String adductDetected = "";

        // Define adduct map for positive and negative ionization modes
        Map<String, String> mapAdducts = getAdductMapByIonizationMode(ionizationMode);
        List<String> allAdductsForCheckRelation = new LinkedList<>(mapAdducts.keySet());

        double adductDouble;
        double adductDoubleForCheckRelation;
        double massToSearchInCompositeSpectrumForCheckRelation;
        double differenceMassAndPeak;

        // ** Hypothesis: adduct is adduct name
        for (String adductName : allAdductsForCheckRelation) {
            String adductValue = mapAdducts.get(adductName);
            if (adductValue == null) {
                break;
            }

            adductDouble = Double.parseDouble(adductValue);

            // Calculate neutral mass based on m/z and adduct
            Double neutralMassBasedOnAdduct = getMassToSearch(mz, adductName, adductDouble);  // Formula for neutral mass from m/z

            // Check relations with other adducts
            for (String adductNameForCheckRelation : allAdductsForCheckRelation) {
                String adductValueForCheckRelation = mapAdducts.get(adductNameForCheckRelation);
                if (adductValueForCheckRelation == null) {
                    break;
                }
                adductDoubleForCheckRelation = Double.parseDouble(adductValueForCheckRelation);
                logger.info("adduct mass check: {}", adductDoubleForCheckRelation);

                if (!adductName.equals(adductNameForCheckRelation)) {
                    // Calculate mass to search in composite spectrum for this adduct
                    massToSearchInCompositeSpectrumForCheckRelation = getMassOfAdductFromMonoWeight(neutralMassBasedOnAdduct, adductNameForCheckRelation, ionizationMode);

                    // ** Hypothesis: Peak
                    for (Double peak : groupedPeaksFiltered.keySet()) {
                        differenceMassAndPeak = Math.abs(peak - massToSearchInCompositeSpectrumForCheckRelation);
                        if (differenceMassAndPeak < Constants.ADDUCT_AUTOMATIC_DETECTION_WINDOW) {
                            adductDetected = adductName;
                            String adductNameFormatted = "[" + adductDetected + "]";
                            if (ionizationMode == IonizationMode.POSITIVE) {
                                adductNameFormatted += "+";
                            } else if (ionizationMode == IonizationMode.NEGATIVE) {
                                adductNameFormatted += "-";
                            }
                            return adductDetected;
                        }
                    }
                }
            }
        }
        String adductNameFormatted = "[" + adductDetected + "]";
        if (ionizationMode == IonizationMode.POSITIVE) {
            adductNameFormatted += "+";
        } else if (ionizationMode == IonizationMode.NEGATIVE) {
            adductNameFormatted += "-";
        }
        return adductDetected;
    }

    /**
     * Retrieves the adduct map based on the ionization mode
     * @param ionizationMode Ionization mode (positive or negative)
     * @return Adduct map for the given ionization mode
     */
    public static Map<String, String> getAdductMapByIonizationMode(IonizationMode ionizationMode) {
        if (ionizationMode == IonizationMode.POSITIVE) {
            return AdductList.MAPMZPOSITIVEADDUCTS;
        } else if (ionizationMode == IonizationMode.NEGATIVE) {
            return AdductList.MAPMZNEGATIVEADDUCTS;
        } else {
            throw new IllegalArgumentException("Unknown ionization mode: " + ionizationMode);
        }
    }

    /**
     * Method to filter the groupedPeaks and filter the adducts.
     * It is specially useful when looking for adduct and fragment in source
     * relations. The Map groupedPeaks should be ordered due to their
     * intensity.
     *
     * @param groupedPeaks TESTED!
     * @return
     */
    public static Map<Double, Double> filterIsotopes(Map<Double, Double> groupedPeaks) {
        Map<Double, Double> deisotopedGroupedPeaks = new TreeMap<>();
        Double previousPeak = 0d;
        for (Map.Entry<Double, Double> entry : groupedPeaks.entrySet()) {
            Double mz = entry.getKey();
            Double intensity = entry.getValue();
            if (previousPeak == 0d || Math.abs(mz - previousPeak) > Constants.BIGGEST_ISOTOPE * Constants.PROTON_WEIGTH) {
                deisotopedGroupedPeaks.put(mz, intensity);
            }
            previousPeak = mz;
        }
        return deisotopedGroupedPeaks;
    }

    /**
     * Calculate the mass to search depending on the adduct hypothesis
     *
     * @param experimentalMass Experimental mass of the compound
     * @param adduct adduct name (M+H, 2M+H, M+2H, etc..)
     * @param adductValue numeric value of the adduct (1.0073, etc..)
     *
     * @return the mass difference within the tolerance respecting to the
     * massToSearch
     */
    public static Double getMassToSearch(Double experimentalMass, String adduct, Double adductValue) {
        Double massToSearch;

        if (AdductList.CHARGE_2.contains(adduct)) {
            massToSearch = getChargedOriginalMass(experimentalMass, adductValue, 2);
        } else if (AdductList.CHARGE_3.contains(adduct)) {
            massToSearch = getChargedOriginalMass(experimentalMass, adductValue, 3);
        } else if (AdductList.DIMER_2.contains(adduct)) {
            massToSearch = getDimmerOriginalMass(experimentalMass, adductValue, 2);
        } else if (AdductList.TRIMER_3.contains(adduct)) {
            massToSearch = getDimmerOriginalMass(experimentalMass, adductValue, 3);
        } else {
            massToSearch = experimentalMass + adductValue;
        }
        return massToSearch;
    }

    public static Double getDimmerOriginalMass(double experimentalMass, double adductValue, int numberAtoms) {
        double result = experimentalMass;

        result = result + adductValue;
        result = result / numberAtoms;

        return result;
    }

    public static Double getChargedAdductMass(double monoisotopicWeight, double adductValue, int charge) {
        double result = monoisotopicWeight;

        result = result / charge;
        result = result - adductValue;

        return result;
    }

    public static Double getChargedOriginalMass(double experimentalMass, double adductValue, int charge) {
        double result = experimentalMass;

        result = result + adductValue;
        result = result * charge;

        return result;
    }

    /**
     * Calculate the adduct Mass based on the monoisotopic weight, without
     * knowing the value of the adduct.
     *
     * @param monoisotopicWeight Experimental mass of the compound
     * @param adduct adduct name (M+H, 2M+H, M+2H, etc..)
     * @param ionizationMode positive, negative or neutral
     *
     * @return the mass difference within the tolerance respecting to the
     * massToSearch
     */
    public static Double getMassOfAdductFromMonoWeight(Double monoisotopicWeight, String adduct, IonizationMode ionizationMode) {
        Double adductValue = getAdductValue(adduct, ionizationMode);
        Double massToSearch;

        if (AdductList.CHARGE_2.contains(adduct)) {
            massToSearch = getChargedAdductMass(monoisotopicWeight, adductValue, 2);
        } else if (AdductList.CHARGE_3.contains(adduct)) {
            massToSearch = getChargedAdductMass(monoisotopicWeight, adductValue, 3);
        } else if (AdductList.DIMER_2.contains(adduct)) {
            massToSearch = getDimmerAdductMass(monoisotopicWeight, adductValue, 2);
        } else if (AdductList.TRIMER_3.contains(adduct)) {
            massToSearch = getDimmerAdductMass(monoisotopicWeight, adductValue, 3);
        } else {
            massToSearch = monoisotopicWeight - adductValue;
        }
        return massToSearch;
    }


    private static Double getDimmerAdductMass(double monoisotopicWeight, double adductValue, int numberAtoms) {
        double result = monoisotopicWeight;
        result = result * numberAtoms;
        result = result - adductValue;

        return result;
    }


    /**
     * Get the value of the adduct named adductName within the ionization mode
     * ionMode
     *
     * @param adductName
     * @param ionizationMode
     * @return
     */
    private static Double getAdductValue(String adductName, IonizationMode ionizationMode) {
        Map<String, String> provisionalMap = getAdductMapByIonizationMode(ionizationMode);
        String adductValue = provisionalMap.get(adductName);;

        if (adductValue == null || adductValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Adduct value not found or is invalid for: " + adductName);
        }

        double adductDouble = Double.parseDouble(adductValue);
        return adductDouble;
    }

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

