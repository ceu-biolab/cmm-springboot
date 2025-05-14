package ceu.biolab.cmm.shared.domain;

public enum FormulaType {
    CHNOPS(false, 0),
    CHNOPSD(true, 1),
    CHNOPSCL(false, 2),
    CHNOPSCLD(true, 3),
    ALL(false, 4),

    ALLD(true, 5);

    private final boolean deuterium;
    private final int formulaTypeIntValue;

    FormulaType(boolean deuterium, int formulaTypeIntValue) {
        this.deuterium = deuterium;
        this.formulaTypeIntValue = formulaTypeIntValue;
    }

    FormulaType(boolean deuterium) {
        this.deuterium = deuterium;
        this.formulaTypeIntValue = 4;
    }


    public boolean includesDeuterium() {
        return deuterium;
    }

    public int getFormulaTypeIntValue() {
        return formulaTypeIntValue;
    }


    public static FormulaType resolveFormulaType(String alphabet, boolean deuterium) {
        String normalizedAlphabet = alphabet.trim().toUpperCase();
        // Append "D" to the alphabet if deuterium is true
        if (deuterium) {
            normalizedAlphabet += "D";
        }
        try {
            return FormulaType.valueOf(normalizedAlphabet);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid combination of alphabet: " + alphabet + " and deuterium: " + deuterium, e);
        }
    }

    /**
     * This method gets the value that corresponds to formula_type_int in compounds_view from the Formula Type formula
     * @param alphabet name of the chamical alphabet as String such as "CHNOPS"
     * @param deuterium whether deuterium is included
     * @return the corresponding integer formula_type_int
     */
    public static int dbValueForFormulaType(String alphabet, boolean deuterium) {
        String normalized = alphabet.trim().toUpperCase();
        for (FormulaType formulaType : FormulaType.values()) {
            if (formulaType.name().equals(normalized) && formulaType.includesDeuterium() == deuterium) {
                return formulaType.getFormulaTypeIntValue();
            }
        }
        throw new IllegalArgumentException("Invalid formula type or deuterium combination: " + alphabet + " (D=" + deuterium + ")");
    }

    public static FormulaType getFormulTypefromInt(int value) {
        for (FormulaType formulaType : FormulaType.values()) {
            if (formulaType.getFormulaTypeIntValue() == value) {
                return formulaType;
            }
        }
        throw new IllegalArgumentException("No FormulaType found for value: " + value);
    }

}
