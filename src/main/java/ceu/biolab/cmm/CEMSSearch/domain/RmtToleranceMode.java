package ceu.biolab.cmm.CEMSSearch.domain;

public enum RmtToleranceMode {
    PERCENTAGE,
    ABSOLUTE;

    public static RmtToleranceMode fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("rmt_tolerance_mode cannot be null");
        }
        return switch (value.trim().toLowerCase()) {
            case "percentage", "percent", "%" -> PERCENTAGE;
            case "absolute" -> ABSOLUTE;
            default -> throw new IllegalArgumentException("Unsupported rmt_tolerance_mode: " + value);
        };
    }
}
