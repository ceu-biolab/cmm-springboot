package ceu.biolab.cmm.shared.service;

import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.shared.domain.compound.Compound;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class FormulaTypeFilter {

    private FormulaTypeFilter() {
    }

    public static Optional<Set<String>> allowedElements(Optional<FormulaType> formulaType) {
        if (formulaType == null || formulaType.isEmpty()) {
            return Optional.empty();
        }
        return allowedElements(formulaType.get());
    }

    public static Optional<Set<String>> allowedElements(FormulaType formulaType) {
        if (formulaType == null) {
            return Optional.empty();
        }
        return formulaType.allowedElements();
    }

    public static boolean matches(Compound compound, Optional<Set<String>> allowedElements) {
        if (allowedElements == null || allowedElements.isEmpty()) {
            return true;
        }
        Optional<Set<String>> compoundElements = compound.formulaElements();
        if (compoundElements.isEmpty()) {
            return true;
        }

        Set<String> normalizedCompoundElements = new LinkedHashSet<>();
        for (String element : compoundElements.get()) {
            normalizedCompoundElements.add(element.toUpperCase(Locale.ROOT));
        }
        return allowedElements.get().containsAll(normalizedCompoundElements);
    }
}
