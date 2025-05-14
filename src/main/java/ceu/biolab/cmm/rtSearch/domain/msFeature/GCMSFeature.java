package ceu.biolab.cmm.rtSearch.domain.msFeature;

import ceu.biolab.cmm.shared.domain.msFeature.MSFeature;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GCMSFeature extends MSFeature {
    private double rt;
    private double ri;

    public GCMSFeature(double mz, double intensity, double rt, double ri) {
        super(mz, intensity);
        this.rt = rt;
        this.ri = ri;
    }


}
