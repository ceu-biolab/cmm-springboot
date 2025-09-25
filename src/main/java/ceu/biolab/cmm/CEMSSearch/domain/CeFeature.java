package ceu.biolab.cmm.CEMSSearch.domain;

import java.util.Optional;

import ceu.biolab.cmm.shared.domain.msFeature.IMSFeature;
import lombok.Data;

@Data
public class CeFeature implements IMSFeature {
    private double mzValue;
    private double effectiveMobility;
    private Optional<Double> intensity;

    public CeFeature(double mzValue, double effectiveMobility) {
        this.mzValue = mzValue;
        this.effectiveMobility = effectiveMobility;
        this.intensity = Optional.empty();
    }

    public CeFeature(double mzValue, double effectiveMobility, Double intensity) {
        this.mzValue = mzValue;
        this.effectiveMobility = effectiveMobility;
        this.intensity = intensity == null ? Optional.empty() : Optional.of(intensity);
    }
}
