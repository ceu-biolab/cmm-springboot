package ceu.biolab.cmm.shared.domain.msFeature;

import java.util.Optional;

import lombok.Data;

@Data
public class MSFeature implements IMSFeature {
    private double mzValue;
    private Optional<Double> intensity;

    public MSFeature() {
        this.mzValue = 0.0;
        this.intensity = Optional.empty();
    }

    public MSFeature(double mzValue) {
        this.mzValue = mzValue;
        this.intensity = Optional.empty();
    }

    public MSFeature(double mzValue, double intensity) {
        this.mzValue = mzValue;
        this.intensity = Optional.of(intensity);
    }
}
