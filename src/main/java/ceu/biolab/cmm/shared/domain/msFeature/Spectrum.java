package ceu.biolab.cmm.shared.domain.msFeature;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Spectrum /*implements IMSFeature*/ {
    private List<Double> mzValues;
    private List<Double> intensityValues;

    public Spectrum(List<Double> mzValues, List<Double> intensityValues) {
        this.mzValues = mzValues != null ? mzValues : new ArrayList<>();
        this.intensityValues = intensityValues != null ? intensityValues : new ArrayList<>();
    }

    public List<Double> getMzValues() {
        return mzValues;
    }

    public void setMzValues(List<Double> mzValues) {
        this.mzValues = this.mzValues = mzValues != null ? mzValues : new ArrayList<>();
    }

    public List<Double> getIntensityValues() {
        return intensityValues;
    }

    public void setIntensityValues(List<Double> intensityValues) {
        this.intensityValues = intensityValues != null ? intensityValues : new ArrayList<>();
    }



}
