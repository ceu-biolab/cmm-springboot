package ceu.biolab.cmm.ccsSearch.domain;

public enum CcsToleranceMode {
    PERCENTAGE("Percentage"),
    ABSOLUTE("Absolute");

    private final String displayName;

    CcsToleranceMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static CcsToleranceMode fromDisplayName(String displayName) {
        for (CcsToleranceMode mode : values()) {
            if (mode.displayName.equalsIgnoreCase(displayName)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("No CcsToleranceMode found for display name: " + displayName);
    }
}
