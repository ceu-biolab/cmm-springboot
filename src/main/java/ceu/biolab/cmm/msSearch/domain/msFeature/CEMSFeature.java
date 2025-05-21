package ceu.biolab.cmm.msSearch.domain.msFeature;

import ceu.biolab.cmm.shared.domain.msFeature.MSFeature;

public class CEMSFeature extends MSFeature {
    private double mt;
    private double effMob;

    public CEMSFeature(double mz, double intensity, double mt, double effMob) {
        super(mz, intensity);
        this.mt = mt;
        this.effMob = effMob;
    }

    public double getMT() {
        return mt;
    }


    public double getEffMob() {
        return effMob;
    }


}
