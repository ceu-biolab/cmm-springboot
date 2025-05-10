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
                    throw new IllegalArgumentException("Adduct not found: " + adductString);
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

        // Loop over all adducts provided
        for (String adductName : adducts) {
            String adductValue = mapAdducts.get(adductName);
            if (adductValue == null) {
                continue;
            }

            adductDouble = Math.abs(Double.parseDouble(adductValue));

            // Calculate neutral mass based on m/z and adduct
            Double neutralMassBasedOnAdduct = mz-adductDouble;  // Formula for neutral mass from m/z

            // Check relations with other adducts
            for (String adductNameForCheckRelation : allAdductsForCheckRelation) {
                String adductValueForCheckRelation = mapAdducts.get(adductNameForCheckRelation);
                if (adductValueForCheckRelation == null) {
                    continue;
                }
                adductDoubleForCheckRelation = Double.parseDouble(adductValueForCheckRelation);
                logger.info("adduct mass check: {}", adductDoubleForCheckRelation);

                if (!adductName.equals(adductNameForCheckRelation)) {
                    // Calculate mass to search in composite spectrum for this adduct
                    String adductNameFormatted = "[" + adductNameForCheckRelation + "]";
                    if (ionizationMode == IonizationMode.POSITIVE) {
                        adductNameFormatted += "+";
                    } else if (ionizationMode == IonizationMode.NEGATIVE) {
                        adductNameFormatted += "-";
                    }

                    logger.info("adduct formatted: {}", adductNameFormatted);
                    massToSearchInCompositeSpectrumForCheckRelation = neutralMassBasedOnAdduct + adductDoubleForCheckRelation;

                    // Loop through peaks in the spectrum to find a match
                    for (Double peak : groupedPeaksFiltered.keySet()) {
                        differenceMassAndPeak = Math.abs(peak - massToSearchInCompositeSpectrumForCheckRelation);
                        if (differenceMassAndPeak < Constants.ADDUCT_AUTOMATIC_DETECTION_WINDOW) {
                            adductDetected = adductName;
                            return adductDetected;
                        }
                    }
                }
            }
        }
        return adductDetected;
    }

    /**
     * Retrieves the adduct map based on the ionization mode
     * @param ionizationMode Ionization mode (positive or negative)
     * @return Adduct map for the given ionization mode
     */
    private static Map<String, String> getAdductMapByIonizationMode(IonizationMode ionizationMode) {
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
}

