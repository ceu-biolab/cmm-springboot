package ceu.biolab.cmm.shared.service;

import ceu.biolab.cmm.shared.domain.Constants;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;

public final class MzToleranceConverter {

    private static final double MDA_TO_DA = 0.001d;

    private MzToleranceConverter() {
        throw new AssertionError("Cannot instantiate MzToleranceConverter");
    }

    public static double toDaltons(MzToleranceMode toleranceMode, double toleranceValue, double ppmReference) {
        if (toleranceMode == null) {
            throw new IllegalArgumentException("m/z tolerance mode is required");
        }
        return switch (toleranceMode) {
            case PPM -> ppmReference * toleranceValue * Constants.PPM_FACTOR;
            case MDA -> toleranceValue * MDA_TO_DA;
        };
    }
}
