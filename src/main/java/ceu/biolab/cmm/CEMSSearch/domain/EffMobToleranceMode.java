package ceu.biolab.cmm.CEMSSearch.domain;

public enum EffMobToleranceMode {
    PERCENTAGE,
    ABSOLUTE;

    public static EffMobToleranceMode fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("eff_mob_tolerance_mode cannot be null");
        }
        return switch (value.trim().toLowerCase()) {
            case "percentage", "percent", "%" -> PERCENTAGE;
            case "absolute" -> ABSOLUTE;
            default -> throw new IllegalArgumentException("Unsupported eff_mob_tolerance_mode: " + value);
        };
    }
}
