package ceu.biolab.cmm.shared.domain;

public enum IonMode {

    POSITIVE("positive"),
    NEGATIVE("negative"),
    NEUTRAL("neutral")
    ;

    private final String value;

    IonMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static IonMode fromString(String value) {
        for (IonMode mode : IonMode.values()) {
            if (mode.value == value) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Invalid IonMode value: " + value);
    }
}
