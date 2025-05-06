package ceu.biolab.cmm.batchAdvancedSearch.domain;

import java.util.Optional;

public enum ChemicalAlphabet {
    CHNOPS(false, 0),
    CHNOPSD(true, 1),
    CHNOPSCL(false, 2),
    CHNOPSCLD(true, 3),
    ALL(false, 4),

    ALLD(true, 5);

    private final boolean deuterium;
    private final int formulaTypeIntValue;

    ChemicalAlphabet(boolean deuterium, int formulaTypeIntValue) {
        this.deuterium = deuterium;
        this.formulaTypeIntValue = formulaTypeIntValue;
    }

    ChemicalAlphabet(boolean deuterium) {
        this.deuterium = deuterium;
        this.formulaTypeIntValue = 4;
    }

    public boolean includesDeuterium() {
        return deuterium;
    }

    public int getFormulaTypeIntValue() {
        return formulaTypeIntValue;
    }

    /**
     * This method gets the value that corresponds to formula_type_int in compounds_view from the ChemicalAlphabet formula
     * @param alphabet name of the chamical alphabet as String such as "CHNOPS"
     * @param deuterium whether deuterium is included
     * @return the corresponding integer formula_type_int
     */
    public static int dbValueForChemAlph(String alphabet, boolean deuterium) {
        String normalized = alphabet.trim().toUpperCase();
        for (ChemicalAlphabet ca : ChemicalAlphabet.values()) {
            if (ca.name().equals(normalized) && ca.includesDeuterium() == deuterium) {
                return ca.getFormulaTypeIntValue();
            }
        }
        throw new IllegalArgumentException("Invalid chemical alphabet or deuterium combination: " + alphabet + " (D=" + deuterium + ")");
    }
}
