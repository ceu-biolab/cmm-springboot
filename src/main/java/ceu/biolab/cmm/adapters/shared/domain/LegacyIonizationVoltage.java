package ceu.biolab.cmm.adapters.shared.domain;

/**
 * Legacy ionization / collision voltage enum used by adapter-layer DTOs.
 *
 * Matches the legacy Ionization_voltage_enum:
 * "low", "medium", "high", "all".
 */
public enum LegacyIonizationVoltage {

    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    ALL("all");

    private final String value;

    LegacyIonizationVoltage(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
