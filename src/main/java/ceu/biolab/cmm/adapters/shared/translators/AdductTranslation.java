package ceu.biolab.cmm.adapters.shared.translators;

import java.util.*;

public class AdductTranslation {

    // Explicit raw -> canonical mappings based on the allowed adduct lists

    private static final Map<String, String> POSITIVE_MAP = Map.ofEntries(
            Map.entry("M+Na", "[M+Na]+"),
            Map.entry("M+2H", "[M+2H]2+"),
            Map.entry("M+H", "[M+H]+"),
            Map.entry("M+K", "[M+K]+"),
            Map.entry("M+NH4", "[M+NH4]+"),
            Map.entry("M+H-H2O", "[M+H-H2O]+"),
            Map.entry("M+H+NH4", "[M+H+NH4]2+"),
            Map.entry("M+H+HCOONa", "[M+H+HCOONa]+"),
            Map.entry("M+H-2H2O", "[M+H-2H2O]+"),
            Map.entry("M+C3H9ONa", "[M+C3H9ONa]+"),
            Map.entry("M+Li", "[M+Li]+"),
            Map.entry("2M+2H+3H2O", "[2M+2H+3H2O]+"),
            Map.entry("2M+ACN+H", "[2M+ACN+H]+"),
            Map.entry("2M+ACN+Na", "[2M+ACN+Na]+"),
            Map.entry("2M+H-H2O", "[2M+H-H2O]+"),
            Map.entry("2M+H", "[2M+H]+"),
            Map.entry("2M+K", "[2M+K]+"),
            Map.entry("2M+NH4", "[2M+NH4]+"),
            Map.entry("2M+Na", "[2M+Na]+"),
            Map.entry("M+2ACN+2H", "[M+2ACN+2H]2+"),
            Map.entry("M+2ACN+H", "[M+2ACN+H]+"),
            Map.entry("M+2H+Na", "[M+2H+Na]3+"),
            Map.entry("M+2K-H", "[M+2K-H]+"),
            Map.entry("M+2Na-H", "[M+2Na-H]+"),
            Map.entry("M+2Na", "[M+2Na]2+"),
            Map.entry("M+3ACN+2H", "[M+3ACN+2H]2+"),
            Map.entry("M+3H", "[M+3H]3+"),
            Map.entry("M+3Na", "[M+3Na]3+"),
            Map.entry("M+ACN+2H", "[M+ACN+2H]2+"),
            Map.entry("M+ACN+H", "[M+ACN+H]+"),
            Map.entry("M+ACN+Na", "[M+ACN+Na]+"),
            Map.entry("M+CH3OH+H", "[M+CH3OH+H]+"),
            Map.entry("M+DMSO+H", "[M+DMSO+H]+"),
            Map.entry("M+H+2Na", "[M+H+2Na]3+"),
            Map.entry("M+H+K", "[M+H+K]2+"),
            Map.entry("M+H+Na", "[M+H+Na]2+"),
            Map.entry("M+IsoProp+H", "[M+IsoProp+H]+"),
            Map.entry("M+IsoProp+Na+H", "[M+IsoProp+Na+H]+"),
            Map.entry("M+NH4-H2O", "[M+NH4-H2O]+")
    );

    private static final Map<String, String> NEGATIVE_MAP = Map.ofEntries(
            Map.entry("M-H", "[M-H]-"),
            Map.entry("M+Cl", "[M+Cl]-"),
            Map.entry("M-H-H2O", "[M-H-H2O]-"),
            Map.entry("M+Na-2H", "[M+Na-2H]-"),
            Map.entry("M+K-2H", "[M+K-2H]-"),
            Map.entry("M+Hac-H", "[M+Hac-H]-"),
            Map.entry("M+FA-H", "[M+FA-H]-"),
            Map.entry("2M+CH3COO", "[2M+CH3COO]-"),
            Map.entry("2M+FA-H", "[2M+FA-H]-"),
            Map.entry("2M+Hac-H", "[2M+Hac-H]-"),
            Map.entry("2M-H", "[2M-H]-"),
            Map.entry("3M-H", "[3M-H]-"),
            Map.entry("M+Br", "[M+Br]-"),
            Map.entry("M+CH3COO", "[M+CH3COO]-"),
            Map.entry("M+TFA-H", "[M+TFA-H]-"),
            Map.entry("M-2H", "[M-2H]2-"),
            Map.entry("M-3H", "[M-3H]3-"),
            Map.entry("M-H2O-H", "[M-H2O-H]-")
    );


    public static String translate(String raw, ceu.biolab.cmm.shared.domain.IonizationMode mode) {
        if (raw == null || mode == null) {
            return null;
        }
        return switch (mode) {
            case POSITIVE -> POSITIVE_MAP.get(raw);
            case NEGATIVE -> NEGATIVE_MAP.get(raw);
            case NEUTRAL -> null;
        };
    }

    public static List<String> translateAll(Collection<String> adducts, ceu.biolab.cmm.shared.domain.IonizationMode mode) {
        List<String> result = new ArrayList<>();
        if (adducts == null) return result;
        for (String adduct : adducts) {
            String translated = translate(adduct, mode);
            if (translated != null) {
                result.add(translated);
            }
        }
        return result;
    }

    public static String reverse(String adduct) {
        if (adduct.startsWith("[") && adduct.endsWith("]") || adduct.endsWith("+") || adduct.endsWith("-")) {
            // Remove brackets and charges
            return adduct.replaceAll("\\[|\\]|\\+|\\-", "");
        }
        return adduct;
    }

    public static List<String> reverseAll(Collection<String> adducts) {
        List<String> result = new ArrayList<>();
        if (adducts == null) return result;
        for (String adduct : adducts) {
            String reversed = reverse(adduct);
            if (reversed != null) {
                result.add(reversed);
            }
        }
        return result;
    }

}
