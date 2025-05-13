package ceu.biolab.cmm.msSearch.domain.msFeature;

import ceu.biolab.cmm.shared.domain.msFeature.MSFeature;

public class GCMSFeature extends MSFeature {
    private double rt;
    private double ri;

    public GCMSFeature(double mz, double intensity, double rt, double ri) {
        super(mz, intensity);
        this.rt = rt;
        this.ri = ri;
    }


}
