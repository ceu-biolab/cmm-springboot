package ceu.biolab.cmm.shared.service.adduct;

import ceu.biolab.cmm.shared.domain.Constants;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.adduct.AdductCatalog;
import ceu.biolab.cmm.shared.domain.adduct.AdductDefinition;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 * Centralised adduct utilities backed exclusively by the CSV definitions.
 */
public final class AdductService {
    private static final Map<IonizationMode, Map<String, Integer>> LEGACY_PRIORITY = Map.of(
            IonizationMode.POSITIVE, ranked(
                    "[M+H]+", "[M+Na]+", "[M+NH4]+", "[M+K]+",
                    "[M+2H]2+", "[M+H+Na]2+", "[M+H+K]2+", "[M+H+NH4]2+",
                    "[M+3H]3+", "[M+2H+Na]3+", "[M+H+2Na]3+", "[M+3Na]3+", "[M+2Na]2+",
                    "[M+ACN+H]+", "[M+ACN+Na]+", "[M+CH3OH+H]+", "[M+DMSO+H]+",
                    "[M+IsoProp+H]+", "[M+IsoProp+Na+H]+", "[M+C3H9ONa]+",
                    "[M+H+HCOONa]+", "[M+H-H2O]+", "[M+H-2H2O]+", "[M+NH4-H2O]+", "[M+Li]+",
                    "[M+2Na-H]+", "[M+2K-H]+",
                    "[M+ACN+2H]2+", "[M+2ACN+2H]2+", "[M+3ACN+2H]2+", "[M+2ACN+H]+",
                    "[2M+H]+", "[2M+Na]+", "[2M+NH4]+", "[2M+K]+",
                    "[2M+ACN+H]+", "[2M+ACN+Na]+", "[2M+H-H2O]+", "[2M+2H+3H2O]+"),
            IonizationMode.NEGATIVE, ranked(
                    "[M-H]-", "[M+Cl]-", "[M+FA-H]-", "[M+Hac-H]-", "[M+CH3COO]-",
                    "[M+Na-2H]-", "[M+K-2H]-", "[M-H-H2O]-",
                    "[M-2H]2-", "[M-3H]3-", "[M+Br]-", "[M+TFA-H]-",
                    "[2M-H]-", "[2M+FA-H]-", "[2M+Hac-H]-", "[2M+CH3COO]-", "[3M-H]-")
    );
    private static final Map<IonizationMode, Map<String, Integer>> EXPLICIT_PRIORITY = Map.of(
            IonizationMode.POSITIVE, ranked(
                    "[M+H]+", "[M+Na]+", "[M+NH4]+", "[M+K]+",
                    "[M+2H]2+", "[M+H-H2O]+", "[2M+H]+", "[M+ACN+H]+",
                    "[M+H-2H2O]+", "[M+NH4-H2O]+"),
            IonizationMode.NEGATIVE, ranked(
                    "[M-H]-", "[M+Cl]-", "[M-H-H2O]-", "[M-2H]2-", "[2M-H]-")
    );

    private AdductService() {
    }

    public static AdductDefinition requireDefinition(IonizationMode ionizationMode, String canonicalAdduct) {
        if (ionizationMode == null) {
            throw new IllegalArgumentException("Ionization mode is required");
        }
        if (canonicalAdduct == null || canonicalAdduct.isBlank()) {
            throw new IllegalArgumentException("Adduct is required and must be in canonical format");
        }
        String normalised = canonicalAdduct.trim();
        AdductDefinition definition = AdductCatalog.definitionsFor(ionizationMode).get(normalised);
        if (definition == null) {
            throw new IllegalArgumentException("Unsupported adduct '" + canonicalAdduct + "' for ionization mode " + ionizationMode);
        }
        return definition;
    }

    public static Set<String> availableAdducts(IonizationMode ionizationMode) {
        return AdductCatalog.definitionsFor(ionizationMode).keySet();
    }

    public static double neutralMassFromMz(double mz, AdductDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        return (mz * definition.absoluteCharge() - definition.offset()) / definition.multimer();
    }

    public static double mzFromNeutralMass(double neutralMass, AdductDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        return (neutralMass * definition.multimer() + definition.offset()) / definition.absoluteCharge();
    }

    public static Map<Double, Double> filterIsotopes(Map<Double, Double> groupedPeaks) {
        Map<Double, Double> deisotoped = new TreeMap<>();
        double previousPeak = 0d;
        for (Map.Entry<Double, Double> entry : groupedPeaks.entrySet()) {
            double mz = entry.getKey();
            double intensity = entry.getValue();
            if (previousPeak == 0d
                    || Math.abs(mz - previousPeak) > Constants.BIGGEST_ISOTOPE * Constants.PROTON_WEIGTH) {
                deisotoped.put(mz, intensity);
                previousPeak = mz;
            }
        }
        return deisotoped;
    }

    public static Optional<AdductDefinition> detectAdduct(IonizationMode ionizationMode,
                                                          double observedMz,
                                                          Set<String> candidateAdducts,
                                                          Map<Double, Double> groupedPeaks) {
        if (groupedPeaks == null || groupedPeaks.isEmpty()) {
            return Optional.empty();
        }
        Set<AdductDefinition> definitions = normalisedDefinitions(ionizationMode, candidateAdducts);
        if (definitions.isEmpty()) {
            return Optional.empty();
        }
        Map<Double, Double> deisotoped = filterIsotopes(groupedPeaks);
        List<AdductDefinition> ordered = definitions.stream()
                .sorted(Comparator.comparing(AdductDefinition::canonical))
                .toList();

        for (AdductDefinition primary : ordered) {
            double neutralMass = neutralMassFromMz(observedMz, primary);
            for (AdductDefinition secondary : ordered) {
                if (primary == secondary) {
                    continue;
                }
                double expectedMz = mzFromNeutralMass(neutralMass, secondary);
                for (Double peak : deisotoped.keySet()) {
                    double delta = Math.abs(peak - expectedMz);
                    if (delta < Constants.ADDUCT_AUTOMATIC_DETECTION_WINDOW) {
                        return Optional.of(primary);
                    }
                }
            }
        }
        return Optional.empty();
    }

    public static List<AdductDefinition> sortByPriority(Set<AdductDefinition> definitions, IonizationMode mode) {
        Map<String, Integer> explicitPriorities = EXPLICIT_PRIORITY.getOrDefault(mode, Map.of());
        Map<String, Integer> legacyPriorities = LEGACY_PRIORITY.getOrDefault(mode, Map.of());
        Comparator<AdductDefinition> comparator = Comparator
                .comparingInt((AdductDefinition def) -> explicitPriorities.getOrDefault(def.canonical(), Integer.MAX_VALUE))
                .thenComparingInt(AdductService::adductGroupPriority)
                .thenComparingInt(def -> legacyPriorities.getOrDefault(def.canonical(), Integer.MAX_VALUE))
                .thenComparing(AdductDefinition::canonical);
        return definitions.stream()
                .sorted(comparator)
                .toList();
    }

    private static Set<AdductDefinition> normalisedDefinitions(IonizationMode mode, Set<String> adducts) {
        if (adducts == null || adducts.isEmpty()) {
            return Set.of();
        }
        Set<AdductDefinition> definitions = new LinkedHashSet<>();
        for (String candidate : adducts) {
            definitions.add(requireDefinition(mode, candidate));
        }
        return definitions;
    }

    public static Map<String, AdductDefinition> definitionMap(IonizationMode ionizationMode) {
        return new LinkedHashMap<>(AdductCatalog.definitionsFor(ionizationMode));
    }

    private static int adductGroupPriority(AdductDefinition definition) {
        if (definition.absoluteCharge() == 1 && definition.multimer() == 1) {
            return 0;
        }
        if (definition.absoluteCharge() == 1) {
            return 1;
        }
        if (definition.absoluteCharge() == 2) {
            return 2;
        }
        if (definition.absoluteCharge() == 3) {
            return 3;
        }
        return 4;
    }

    private static Map<String, Integer> ranked(String... canonicalOrder) {
        Map<String, Integer> priorities = new LinkedHashMap<>();
        for (int i = 0; i < canonicalOrder.length; i++) {
            priorities.put(canonicalOrder[i], i);
        }
        return priorities;
    }
}
