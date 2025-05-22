package ceu.biolab.cmm.shared.domain.msFeature;

import lombok.Data;

import java.util.Optional;
import lombok.Data;

@Data
public class Peak {
    private double mzValue;
    private double intensity;


    public Peak(double mzValue, double intensity) {
        this.mzValue = mzValue;
        this.intensity = intensity;
    }
}