package ceu.biolab.cmm.ccsSearch.domain;

import java.util.Optional;

import ceu.biolab.cmm.shared.domain.msFeature.IMSFeature;
import lombok.Data;

@Data
public class IMFeature implements IMSFeature {
    private double mzValue;
    private double ccsValue;
    private Optional<Double> intensity;

    public IMFeature(double mzValue, double ccsValue) {
        this.mzValue = mzValue;
        this.ccsValue = ccsValue;
        this.intensity = Optional.empty();
    }

    public IMFeature(double mzValue, double ccsValue, double intensity) {
        this.mzValue = mzValue;
        this.ccsValue = ccsValue;
        this.intensity = Optional.of(intensity);
    }
}
