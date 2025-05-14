package ceu.biolab.cmm.rtSearch.domain.msFeature;

import ceu.biolab.cmm.shared.domain.msFeature.MSFeature;

import java.util.Objects;

public class LCMSFeatureImpl extends MSFeature implements LCMSFeature{

    private final double rt;

    public LCMSFeatureImpl(double mz, double intensity, double rt) {
        super(mz, intensity);
        this.rt = rt;
    }

    @Override
    public double getRT() {
        return this.rt;
    }

    public double getRt() {
        return rt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LCMSFeatureImpl that = (LCMSFeatureImpl) o;
        if(super.equals(o)) {
            return Double.compare(that.rt, rt) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), rt);
    }

    @Override
    public String toString() {
        return "LCMSFeatureImpl{" + "rt=" + this.rt + '}';
    }





}
