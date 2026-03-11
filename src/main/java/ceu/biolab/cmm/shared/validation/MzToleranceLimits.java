package ceu.biolab.cmm.shared.validation;

import ceu.biolab.cmm.shared.domain.MzToleranceMode;

public final class MzToleranceLimits {

    public static final double MAX_PPM = 100.0d;
    public static final double MAX_MDA = 100.0d;

    private MzToleranceLimits() {
        throw new AssertionError("Cannot instantiate MzToleranceLimits");
    }

    public static boolean exceedsLimit(double tolerance, MzToleranceMode mode) {
        return tolerance > maxAllowed(mode);
    }

    public static double maxAllowed(MzToleranceMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("m/z tolerance mode is required");
        }
        return switch (mode) {
            case PPM -> MAX_PPM;
            case MDA -> MAX_MDA;
        };
    }

    public static String violationMessage(String fieldName, MzToleranceMode mode) {
        String safeFieldName = fieldName == null || fieldName.isBlank() ? "tolerance" : fieldName;
        return safeFieldName + " must be less than or equal to " + trim(maxAllowed(mode)) + " " + mode + ".";
    }

    private static String trim(double value) {
        if (value == Math.rint(value)) {
            return Integer.toString((int) value);
        }
        return Double.toString(value);
    }
}
