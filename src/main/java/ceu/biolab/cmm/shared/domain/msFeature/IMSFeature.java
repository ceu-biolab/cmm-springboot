package ceu.biolab.cmm.shared.domain.msFeature;

import java.util.Optional;

public interface IMSFeature {
    double getMzValue();

    void setMzValue(double mzValue);

    Optional<Double> getIntensity();

    void setIntensity(Optional<Double> intensity);
}
