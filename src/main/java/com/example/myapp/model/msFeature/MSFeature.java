package com.example.myapp.model.msFeature;

import com.example.myapp.model.compound.GroupedCompoundsByAdduct;

import java.util.Objects;
import java.util.Set;

public class MSFeature {

    private final double mz;
    private final double intensity;
    private Set<GroupedCompoundsByAdduct> potentialAnnotations;

    public MSFeature(double mz, double intensity) {
        this.mz = mz;
        this.intensity = intensity;
    }

    public double getMz() {
        return mz;
    }

    public double getIntensity() {
        return intensity;
    }



    @Override
    public String toString() {
        return "MSFeature{" +
                "mz=" + mz +
                ", intensity=" + intensity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MSFeature msFeature = (MSFeature) o;
        return Double.compare(msFeature.mz, mz) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mz);
    }

}
