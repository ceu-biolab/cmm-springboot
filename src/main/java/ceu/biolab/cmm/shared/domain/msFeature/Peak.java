package ceu.biolab.cmm.shared.domain.msFeature;

import lombok.Data;

import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class Peak {
    private double mzValue;
    private double intensity;

    public Peak(){
        this.mzValue = -1;
        this.intensity = -1;
    }

    public Peak(double mzValue, double intensity) {
        this.mzValue = mzValue;
        this.intensity = intensity;
    }
}