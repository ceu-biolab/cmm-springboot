package ceu.biolab.cmm.CEMSMarkers.domain;

public enum MassMode {
    MZ("m/z"),
    MASS("Neutral Masses");

    private final String cemsEquivalent;

    MassMode(String cemsEquivalent) {
        this.cemsEquivalent = cemsEquivalent;
    }

    public String getCemsEquivalent() {
        return cemsEquivalent;
    }

    public static MassMode fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("mass_mode cannot be null");
        }
        return switch (value.trim().toLowerCase()) {
            case "mz", "m/z" -> MZ;
            case "mass", "neutral", "neutral_masses" -> MASS;
            default -> throw new IllegalArgumentException("Unsupported mass_mode: " + value);
        };
    }
}
