package ceu.biolab.cmm.shared.domain;

public enum IonizationMode {

    POSITIVE("positive"),
    NEGATIVE("negative"),
    NEUTRAL("neutral")
    ;

    private final String value;

    IonizationMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static IonizationMode fromString(String value) {
        for (IonizationMode mode : IonizationMode.values()) {
            if (mode.value == value) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Invalid IonMode value: " + value);
    }
}
