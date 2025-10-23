package ceu.biolab.cmm.scoreAnnotations.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LipidScores extends CompoundScores {

    public LipidScores() {
        super();
    }

    public static String calculateFeatureKey(double featureMzValue, double featureRtValue) {
        return CompoundScores.calculateFeatureKey(featureMzValue, featureRtValue);
    }
}
