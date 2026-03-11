package ceu.biolab.cmm.adapters.shared.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LegacyPeak {
    @JsonProperty("mz")
    private double mz;
    private double intensity;

    public LegacyPeak() {
        this.mz = -1;
        this.intensity = -1;
    }

    public LegacyPeak(double mz, double intensity) {
        this.mz = mz;
        this.intensity = intensity;
    }
}
