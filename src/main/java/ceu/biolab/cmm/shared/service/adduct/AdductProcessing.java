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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class AdductProcessing {
    private static final Logger logger = LoggerFactory.getLogger(CompoundRepository.class);

    public static Adduct getAdductFromString(String adductString, IonizationMode ionizationMode, Double mz) throws IncorrectAdduct {
        try {
            if (ionizationMode == IonizationMode.POSITIVE) {
                if (AdductList.MAPMZPOSITIVEADDUCTS.containsKey(adductString)) {
                    int charge = AdductTransformer.getChargeOfAdduct(adductString);
                    String adductFormula = "[" + adductString + "]" + charge + "+";
                    Adduct adj = new Adduct((adductFormula));
                    return adj;
                } else
                    throw new IllegalArgumentException("Adduct not found: " + adductString);
            } else if (AdductList.MAPMZNEGATIVEADDUCTS.containsKey(adductString)) {
                int charge = AdductTransformer.getChargeOfAdduct(adductString);
                String adductFormula = "[" + adductString + "]" + charge + "-";
                return new Adduct(adductFormula);
            } else {
                throw new IllegalArgumentException("Adduct not found: " + adductString);
            }
        } catch (NotFoundElement | IncorrectFormula | IncorrectAdduct e) {
            throw new IncorrectAdduct("Invalid adduct: " + adductString + e.getMessage());
        }
    }


    /**
     * Detect the ionization adduct based on the relationships in the Composite
     * Spectrum. The mz is handled as m/z
     *
     * @param ionizationMode Ionization Mode (enum): Positive or Negative
     * @param mz             experimental masses (m/z)
     * @param adducts        possible adducts to be formed as Set
     * @param groupedPeaks   Signals of the metabolite M
     * @return
     */
    public static String detectAdductBasedOnCompositeSpectrum(IonizationMode ionizationMode, Double mz,
                                                              Set<String> adducts, Map<Double, Double> groupedPeaks) {
        if (groupedPeaks.isEmpty()) {
            return "";
        }

        logger.info("Adducts: {}", adducts);

        Map<Double, Double> groupedPeaksFiltered = filterIsotopes(groupedPeaks);
        String adductDetected = "";
        Map<String, String> mapAdducts = getAdductMapByIonizationMode(ionizationMode);
        List<String> allAdductsForCheckRelation = new ArrayList<>(mapAdducts.keySet());

        // Commented out because it was not used
        // double adductDouble;
        // double adductDoubleForCheckRelation;
        double massToSearchInCompositeSpectrumForCheckRelation;
        double differenceMassAndPeak;

        for (String adductName : adducts) {    //** Hypothesis -> Adduct is adductName
            logger.info("Adduct Name: {}", adductName);
            String adductValue = mapAdducts.get(adductName);
            if (adductValue == null) {
                break;
            }
            // adductDouble = Math.abs(Double.parseDouble(adductValue));
            Double neutralMassBasedOnAdduct = AdductTransformer.getMonoisotopicMassFromMZ(mz, adductName, ionizationMode);

            //** Hypothesis -> Peak is adductName
            // Peak to search in Composite Spectrum is now in massToSearchInCompositeSpectrum
            // So now is time to loop the composite spectrum searching the peak

            for (String adductNameForCheckRelation : allAdductsForCheckRelation) {
                String adductValueForCheckRelation = mapAdducts.get(adductNameForCheckRelation);
                if (adductValueForCheckRelation == null) {
                    break;
                }
                // adductDoubleForCheckRelation = Double.parseDouble(adductValueForCheckRelation);
                if (!adductName.equals(adductNameForCheckRelation)) {
                    // get MZFomMonoisotopicMass
                    String adductNameFormatted = "[" + adductNameForCheckRelation + "]";
                    if(ionizationMode == IonizationMode.POSITIVE){
                        adductNameFormatted+= "+";
                    }else if(ionizationMode == IonizationMode.NEGATIVE){
                        adductNameFormatted+= "-";
                    }
                    massToSearchInCompositeSpectrumForCheckRelation = AdductTransformer.getMassOfAdductFromMonoMass(neutralMassBasedOnAdduct, adductNameFormatted, ionizationMode);

                    // Peak to search in Composite Spectrum is now in massToSearchInCompositeSpectrum
                    // So now is time to loop the composite spectrum searching the peak
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
        logger.info("Adduct detected: {}", adductDetected);

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
     * Method to filter the groupedPeaks and filter the adducts. It is
     * specially useful when looking for adduct and fragment in source
     * relations. The Map groupedPeaks should be ordered due to their
     * intensity.
     *
     * @param groupedPeaks TESTED!
     * @return
     */
    public static Map<Double, Double> filterIsotopes(Map<Double, Double> groupedPeaks) {
        Map<Double, Double> deisotopedGroupedPeaks = new TreeMap<Double, Double>();
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
