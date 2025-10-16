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
import java.util.stream.Collectors;

/**
 * Centralised adduct utilities backed exclusively by the CSV definitions.
 */
public final class AdductService {
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
}
