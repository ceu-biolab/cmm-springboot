package ceu.biolab.cmm.ccsSearch.domain;

import ceu.biolab.cmm.shared.domain.msFeature.ILCMSFeature;

public class IMLCMSFeature extends IMFeature implements ILCMSFeature {
    private double rtValue;

    public IMLCMSFeature(double mzValue, double ccsValue, double rtValue) {
        super(mzValue, ccsValue);
        this.rtValue = rtValue;
    }

    @Override
    public double getRtValue() {
        return rtValue;
    }

    @Override
    public void setRtValue(double rtValue) {
        this.rtValue = rtValue;
    }

}
