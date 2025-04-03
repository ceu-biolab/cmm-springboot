package ceu.biolab.cmm.shared.domain.msFeature;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LCMSFeature extends MSFeature implements ILCMSFeature {
    private double rtValue;

    public LCMSFeature() {
        super();
        this.rtValue = 0.0;
    }

    public LCMSFeature(double rtValue, double mzValue) {
        super();
        this.rtValue = rtValue;
        this.setMzValue(mzValue);
    }

    public LCMSFeature(double rtValue, double mzValue, double intensity) {
        super(mzValue, intensity);
        this.rtValue = rtValue;
    }
}
