package ceu.biolab.cmm.adapters.shared.domain;

/**
 * Legacy ionization mode enum used by adapter-layer DTOs.
 *
 * Matches the legacy ion_mode_enum: "positive", "negative".
 * This is a subset of the shared IonizationMode enum.
 */
public enum LegacyIonMode {

    POSITIVE("positive"),
    NEGATIVE("negative");

    private final String value;

    LegacyIonMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
