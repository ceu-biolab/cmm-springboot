package ceu.biolab.cmm.shared.domain.adduct;

import ceu.biolab.Adduct;
import ceu.biolab.IncorrectAdduct;
import ceu.biolab.IncorrectFormula;
import ceu.biolab.NotFoundElement;
import ceu.biolab.cmm.shared.domain.IonizationMode;

import java.util.Map;

public class AdductProcessing {

    public static Adduct getAdductFromString(String adductString, IonizationMode ionizationMode, Double mz) throws IncorrectAdduct {
        try {
            if (ionizationMode == IonizationMode.POSITIVE) {
                if (AdductList.MAPMZPOSITIVEADDUCTS.containsKey(adductString)) {
                    String adductFormula = "[" + adductString + "]+";
                    return new Adduct(adductFormula);
                } else
                    throw new IllegalArgumentException("Adduct not found: " + adductString);
            }else if (AdductList.MAPMZNEGATIVEADDUCTS.containsKey(adductString)) {
                String adductFormula = "[" + adductString + "]-";
                return new Adduct(adductFormula);
                } else {
                throw new IllegalArgumentException("Adduct not found: " + adductString);
                }
        } catch (NotFoundElement | IncorrectFormula | IncorrectAdduct e) {
            throw new IncorrectAdduct("Invalid adduct: " + adductString + e.getMessage());
        }
    }

    public static Map<String, String> getAdduct(String adductString, IonizationMode ionizationMode) {
        Map<String, String> adductMap;

        if (ionizationMode == IonizationMode.POSITIVE) {
            adductMap = AdductList.MAPMZPOSITIVEADDUCTS;
        } else {
            adductMap = AdductList.MAPMZNEGATIVEADDUCTS;
        }

        if (adductMap.containsKey(adductString)) {
            return Map.of(adductString, "");
        } else {
            adductMap.put(adductString, "");
            return Map.of(adductString, "");
        }
    }


}
