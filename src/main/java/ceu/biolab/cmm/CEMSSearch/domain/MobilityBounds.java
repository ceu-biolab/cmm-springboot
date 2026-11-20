package ceu.biolab.cmm.CEMSSearch.domain;

/** Exact mobility interval derived from a marker-calibrated time interval. */
public record MobilityBounds(double lower, double upper) {
    public MobilityBounds {
        if (!Double.isFinite(lower) || !Double.isFinite(upper) || lower > upper) {
            throw new IllegalArgumentException("Mobility bounds must be finite and ordered");
        }
    }
}
