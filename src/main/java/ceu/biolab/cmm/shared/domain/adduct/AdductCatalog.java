package ceu.biolab.cmm.shared.domain.adduct;

import ceu.biolab.cmm.shared.domain.IonizationMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Loads the canonical adduct definitions from CSV resources and exposes them for lookup.
 */
public final class AdductCatalog {
    private static final Pattern CANONICAL_PATTERN =
            Pattern.compile("^\\[(?<body>[^]]+)](?:(?<chargeDigits>\\d+)?(?<sign>[+-]))$");
    private static final Pattern BODY_PATTERN =
            Pattern.compile("^(?<multimer>\\d*)M(?<descriptor>.*)$");

    private static final Map<IonizationMode, Map<String, AdductDefinition>> DEFINITIONS;

    static {
        Map<IonizationMode, Map<String, AdductDefinition>> byMode = new EnumMap<>(IonizationMode.class);
        byMode.put(IonizationMode.POSITIVE, load("/adducts/adducts_positive_mode.csv", IonizationMode.POSITIVE));
        byMode.put(IonizationMode.NEGATIVE, load("/adducts/adducts_negative_mode.csv", IonizationMode.NEGATIVE));
        byMode.put(IonizationMode.NEUTRAL, Collections.emptyMap());
        DEFINITIONS = Collections.unmodifiableMap(byMode);
    }

    private AdductCatalog() {
    }

    public static Map<String, AdductDefinition> definitionsFor(IonizationMode ionizationMode) {
        Map<String, AdductDefinition> definitions = DEFINITIONS.get(ionizationMode);
        if (definitions == null) {
            return Collections.emptyMap();
        }
        return definitions;
    }

    private static Map<String, AdductDefinition> load(String resourcePath, IonizationMode ionizationMode) {
        try (InputStream stream = AdductCatalog.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalStateException("Missing adduct resource: " + resourcePath);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                return reader.lines()
                        .skip(1) // header
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .map(line -> parseLine(line, ionizationMode))
                        .collect(Collectors.toUnmodifiableMap(AdductDefinition::canonical, Function.identity()));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load adduct definitions from " + resourcePath, e);
        }
    }

    private static AdductDefinition parseLine(String csvLine, IonizationMode ionizationMode) {
        String[] parts = splitCsvLine(csvLine);
        if (parts.length != 2) {
            throw new IllegalStateException("Unexpected adduct CSV format: " + csvLine);
        }
        String canonicalRaw = parts[0].trim();
        if (canonicalRaw.startsWith("\"") && canonicalRaw.endsWith("\"") && canonicalRaw.length() >= 2) {
            canonicalRaw = canonicalRaw.substring(1, canonicalRaw.length() - 1);
        }
        String canonical = canonicalRaw.trim();
        double offset;
        try {
            offset = Double.parseDouble(parts[1].trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid adduct mass in CSV: " + csvLine, e);
        }

        Matcher canonicalMatcher = CANONICAL_PATTERN.matcher(canonical);
        if (!canonicalMatcher.matches()) {
            throw new IllegalStateException("Adduct does not follow canonical format [nM+X]z±: " + canonical);
        }
        String body = canonicalMatcher.group("body");
        String chargeDigits = canonicalMatcher.group("chargeDigits");
        String sign = canonicalMatcher.group("sign");

        Matcher bodyMatcher = BODY_PATTERN.matcher(body);
        if (!bodyMatcher.matches()) {
            throw new IllegalStateException("Unable to parse canonical adduct body: " + canonical);
        }
        String multimerDigits = bodyMatcher.group("multimer");
        String descriptor = bodyMatcher.group("descriptor");
        int multimer = multimerDigits == null || multimerDigits.isBlank() ? 1 : Integer.parseInt(multimerDigits);
        int chargeMagnitude = chargeDigits == null || chargeDigits.isBlank() ? 1 : Integer.parseInt(chargeDigits);
        int signValue = "+".equals(sign) ? 1 : -1;
        int charge = chargeMagnitude * signValue;

        validateChargeAgainstMode(canonical, ionizationMode, charge);

        return new AdductDefinition(canonical,
                ionizationMode,
                multimer,
                descriptor == null ? "" : descriptor,
                charge,
                offset);
    }

    private static void validateChargeAgainstMode(String canonical,
                                                  IonizationMode ionizationMode,
                                                  int charge) {
        if (ionizationMode == IonizationMode.POSITIVE && charge <= 0) {
            throw new IllegalStateException("Positive-mode adduct must have a positive charge: " + canonical);
        }
        if (ionizationMode == IonizationMode.NEGATIVE && charge >= 0) {
            throw new IllegalStateException("Negative-mode adduct must have a negative charge: " + canonical);
        }
    }

    private static String[] splitCsvLine(String csvLine) {
        // CSV is simple (no escaped commas), so a lightweight split is sufficient.
        int commaIndex = csvLine.indexOf(',');
        if (commaIndex < 0) {
            return new String[]{csvLine};
        }
        String first = csvLine.substring(0, commaIndex);
        String second = commaIndex + 1 < csvLine.length() ? csvLine.substring(commaIndex + 1) : "";
        return new String[]{first, second};
    }
}
