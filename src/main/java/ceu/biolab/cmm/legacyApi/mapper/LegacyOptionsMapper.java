package ceu.biolab.cmm.legacyApi.mapper;

import ceu.biolab.cmm.legacyApi.dto.LegacySearchOptions;
import ceu.biolab.cmm.shared.domain.*;
import ceu.biolab.cmm.shared.domain.adduct.AdductCatalog;
import ceu.biolab.cmm.shared.domain.adduct.AdductDefinition;
import ceu.biolab.cmm.shared.validation.MzToleranceLimits;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;

/** Legacy spelling and representation policy. Scientific definitions come from the shared catalog. */
@Component
public class LegacyOptionsMapper {
    private final Validator validator;
    public LegacyOptionsMapper(Validator validator) { this.validator = validator; }

    public <T> T validated(T value) {
        var violations = validator.validate(value);
        if (!violations.isEmpty()) {
            throw bad(violations.stream().map(v -> v.getPropertyPath() + ": " + v.getMessage()).sorted().findFirst().orElseThrow());
        }
        return value;
    }
    public static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
    public static String key(String value) {
        if (value == null || value.isBlank()) throw bad("A nonblank option is required");
        return value.trim().toLowerCase(Locale.ROOT);
    }
    public IonizationMode ionMode(String value) {
        return switch (key(value)) {
            case "positive" -> IonizationMode.POSITIVE;
            case "negative" -> IonizationMode.NEGATIVE;
            default -> throw bad("Unsupported ion_mode: " + value);
        };
    }
    public MzToleranceMode toleranceMode(String value) {
        return switch (key(value)) {
            case "ppm" -> MzToleranceMode.PPM;
            case "mda" -> MzToleranceMode.MDA;
            default -> throw bad("Unsupported tolerance_mode: " + value);
        };
    }
    public double tolerance(Double value, String mode) {
        var parsed = toleranceMode(mode);
        if (value == null || !Double.isFinite(value) || value <= 0 || MzToleranceLimits.exceedsLimit(value, parsed)) {
            throw bad("Invalid tolerance for " + mode);
        }
        return value;
    }
    public double mz(double mass, LegacySearchOptions options) {
        if (!Double.isFinite(mass) || mass <= 0) throw bad("Mass must be finite and positive");
        var mode = ionMode(options.getIonMode());
        double mz = switch (key(options.getMassesMode())) {
            case "mz" -> mass;
            case "neutral" -> mass + (mode == IonizationMode.POSITIVE ? Constants.PROTON_WEIGTH : -Constants.PROTON_WEIGTH);
            default -> throw bad("Unsupported masses_mode: " + options.getMassesMode());
        };
        if (mz <= 0) throw bad("Converted m/z must be positive");
        return mz;
    }
    public Set<Database> databases(Set<String> values) {
        if (values == null || values.isEmpty()) throw bad("databases must not be empty");
        Set<Database> result = new LinkedHashSet<>();
        for (String value : values) {
            if (key(value).equals("all-except-mine")) result.add(Database.ALL);
            else result.add(Arrays.stream(Database.values()).filter(d -> d.getName().equalsIgnoreCase(value))
                    .findFirst().orElseThrow(() -> bad("Unsupported database: " + value)));
        }
        return result;
    }
    public MetaboliteType metaboliteType(String value) {
        return switch (key(value)) {
            case "only-lipids" -> MetaboliteType.ONLYLIPIDS;
            case "all", "all-except-peptides", "all-including-peptides" -> MetaboliteType.ALL;
            default -> throw bad("Unsupported metabolites_type: " + value);
        };
    }
    public Set<String> adducts(LegacySearchOptions options) {
        var mode = ionMode(options.getIonMode());
        var definitions = AdductCatalog.definitionsFor(mode);
        Set<String> result = new LinkedHashSet<>();
        if (options.getAdducts() == null || options.getAdducts().isEmpty()) throw bad("adducts must not be empty");
        for (String raw : options.getAdducts()) {
            String value = key(raw);
            if (value.equals("all") || (mode == IonizationMode.POSITIVE && value.equals("allpositives"))
                    || (mode == IonizationMode.NEGATIVE && value.equals("allnegatives"))) result.addAll(definitions.keySet());
            else result.add(definitions.values().stream()
                    .filter(d -> d.canonical().equals(raw) || d.legacyKey().equals(raw))
                    .map(AdductDefinition::canonical).findFirst()
                    .orElseThrow(() -> bad("Unsupported adduct for " + options.getIonMode() + ": " + raw)));
        }
        return result;
    }
    public String legacyAdduct(String canonical, IonizationMode mode) {
        var definition = AdductCatalog.definitionsFor(mode).get(canonical);
        if (definition == null) throw new IllegalStateException("Unknown adduct returned by search: " + canonical);
        return definition.legacyKey();
    }
    public FormulaType alphabet(String value, boolean deuterium) {
        return FormulaType.resolveFormulaType(value, deuterium);
    }
}
