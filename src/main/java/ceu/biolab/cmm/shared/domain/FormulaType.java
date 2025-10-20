package ceu.biolab.cmm.shared.domain;

public enum FormulaType {
    CHNOPS(false, 0),
    CHNOPSD(true, 1),
    CHNOPSCL(false, 2),
    CHNOPSCLD(true, 3),
    ALL(false, 4),

    ALLD(true, 5);

    private static final java.util.Map<FormulaType, java.util.Set<String>> ELEMENTS_BY_TYPE;
    private static final java.util.regex.Pattern FORMULA_ELEMENT_PATTERN = java.util.regex.Pattern.compile("([A-Z][a-z]?)");

    static {
        java.util.Map<FormulaType, java.util.Set<String>> map = new java.util.EnumMap<>(FormulaType.class);
        map.put(CHNOPS, java.util.Set.of("C", "H", "N", "O", "P", "S"));
        map.put(CHNOPSD, map.get(CHNOPS));
        map.put(CHNOPSCL, java.util.Set.of("C", "H", "N", "O", "P", "S", "CL"));
        map.put(CHNOPSCLD, map.get(CHNOPSCL));
        ELEMENTS_BY_TYPE = java.util.Collections.unmodifiableMap(map);
    }

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
        return resolveFormulaType(alphabet, deuterium).getFormulaTypeIntValue();
    }

    public static FormulaType getFormulaTypefromInt(int value) {
        for (FormulaType formulaType : FormulaType.values()) {
            if (formulaType.getFormulaTypeIntValue() == value) {
                return formulaType;
            }
        }
        throw new IllegalArgumentException("No FormulaType found for value: " + value);
    }

    public static java.util.Optional<FormulaType> inferFromFormula(String formula) {
        if (formula == null || formula.isBlank()) {
            return java.util.Optional.empty();
        }

        java.util.Set<String> elements = new java.util.LinkedHashSet<>();
        java.util.regex.Matcher matcher = FORMULA_ELEMENT_PATTERN.matcher(formula);
        boolean containsDeuterium = false;
        while (matcher.find()) {
            String element = matcher.group(1).toUpperCase(java.util.Locale.ROOT);
            if ("D".equals(element)) {
                containsDeuterium = true;
            } else {
                elements.add(element);
            }
        }
        if (elements.isEmpty() && !containsDeuterium) {
            return java.util.Optional.empty();
        }

        java.util.List<FormulaType> candidates = java.util.Arrays.stream(values())
                .filter(type -> type != ALL && type != ALLD)
                .sorted(java.util.Comparator.comparingInt(type -> ELEMENTS_BY_TYPE.getOrDefault(type, java.util.Set.of()).size()))
                .toList();

        for (FormulaType candidate : candidates) {
            if (candidate.includesDeuterium() != containsDeuterium) {
                continue;
            }
            java.util.Set<String> allowed = ELEMENTS_BY_TYPE.getOrDefault(candidate, java.util.Set.of());
            if (allowed.containsAll(elements)) {
                return java.util.Optional.of(candidate);
            }
        }

        return java.util.Optional.of(containsDeuterium ? ALLD : ALL);
    }

}
