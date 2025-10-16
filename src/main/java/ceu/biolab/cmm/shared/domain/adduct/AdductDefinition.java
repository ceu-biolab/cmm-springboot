package ceu.biolab.cmm.shared.domain.adduct;

import ceu.biolab.cmm.shared.domain.IonizationMode;

import java.util.Objects;

/**
 * Represents a single adduct definition coming from the canonical CSV source.
 */
public final class AdductDefinition {
    private final String canonical;
    private final IonizationMode ionizationMode;
    private final int multimer;
    private final String descriptor;
    private final int charge;
    private final double offset;

    AdductDefinition(String canonical,
                     IonizationMode ionizationMode,
                     int multimer,
                     String descriptor,
                     int charge,
                     double offset) {
        this.canonical = Objects.requireNonNull(canonical, "canonical");
        this.ionizationMode = Objects.requireNonNull(ionizationMode, "ionizationMode");
        this.multimer = multimer;
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.charge = charge;
        this.offset = offset;
    }

    public String canonical() {
        return canonical;
    }

    public IonizationMode ionizationMode() {
        return ionizationMode;
    }

    /**
     * Returns the number of monomer units (n) in the adduct, e.g. 2 for [2M+Na]+.
     */
    public int multimer() {
        return multimer;
    }

    /**
     * Returns the portion of the adduct inside the brackets without the nM prefix.
     */
    public String descriptor() {
        return descriptor;
    }

    /**
     * Signed charge (z) of the adduct (positive values for cations, negative for anions).
     */
    public int charge() {
        return charge;
    }

    public int absoluteCharge() {
        return Math.abs(charge);
    }

    /**
     * Mass offset associated with the adduct in Daltons.
     */
    public double offset() {
        return offset;
    }

    public boolean isPositive() {
        return charge > 0;
    }

    public boolean isNegative() {
        return charge < 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AdductDefinition other)) {
            return false;
        }
        return canonical.equals(other.canonical);
    }

    @Override
    public int hashCode() {
        return canonical.hashCode();
    }

    @Override
    public String toString() {
        return canonical;
    }
}
