package ceu.biolab.cmm.msSearch.model.msFeature;

import java.util.Objects;

public class MSPeak {
    private final double mz;
    private double intensity;

    public MSPeak(double mz, double intensity) {
        this.mz = mz;
        this.intensity = intensity;
    }

    public double getMz() {
        return mz;
    }

    public double getIntensity() {
        return intensity;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MSPeak msPeak = (MSPeak) o;
        return Double.compare(msPeak.mz, mz) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mz);
    }

    @Override
    public String toString() {
        return "MSPeak{" + "mz=" + this.mz +
                ", intensity=" + this.intensity + '}';
    }

}
