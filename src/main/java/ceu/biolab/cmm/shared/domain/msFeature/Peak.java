package ceu.biolab.cmm.shared.domain.msFeature;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class Peak {
    @Positive
    private double mzValue;
    @PositiveOrZero
    private double intensity;

    public Peak(){
        this.mzValue = 0.0;
        this.intensity = 0.0;
    }

    public Peak(double mzValue, double intensity) {
        this.mzValue = mzValue;
        this.intensity = intensity;
    }
}
