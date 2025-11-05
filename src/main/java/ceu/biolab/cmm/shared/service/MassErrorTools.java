package ceu.biolab.cmm.shared.service;

/**
 * Utility helpers for mass error calculations.
 */
public final class MassErrorTools {

    private static final double ONE_MILLION = 1_000_000.0d;

    private MassErrorTools() {
        // utility class
    }

    /**
     * Computes the parts-per-million (ppm) difference between a candidate mass and the target mass.
     *
     * @param candidateMass the candidate neutral mass value (e.g. from a reference compound)
     * @param targetMass the target neutral mass value derived from the experimental measurement
     * @return the signed ppm error, or {@code null} if inputs are null or invalid
     */
    public static Double computePpm(Double candidateMass, Double targetMass) {
        if (candidateMass == null || targetMass == null) {
            return null;
        }
        if (targetMass == 0d) {
            return null;
        }
        return ((candidateMass - targetMass) / targetMass) * ONE_MILLION;
    }
}
