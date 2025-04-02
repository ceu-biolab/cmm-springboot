package ceu.biolab.cmm.scoreAnnotations.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EvaluatedLipid {
    private Lipid lipid;
    private String featureKey;
    private double featureMz;
    private double featureRtValue;
    private LipidScores scores;
    private boolean isSignificative;

    public double getFeatureRtValue() {
        return featureRtValue;
    }

    public EvaluatedLipid(Lipid lipid, double featureMz, double featureRtValue, LipidScores scores, boolean isSignificative) {
        this.lipid = lipid;
        this.featureMz = featureMz;
        this.featureRtValue = featureRtValue;
        this.scores = scores;
        this.isSignificative = isSignificative;

        this.featureKey = String.valueOf(featureMz) + String.valueOf(featureRtValue);
    }

    public EvaluatedLipid(Lipid lipid, double featureMz, double featureRtValue, LipidScores scores) {
        this.lipid = lipid;
        this.featureMz = featureMz;
        this.featureRtValue = featureRtValue;
        this.scores = scores;
        this.isSignificative = true;

        this.featureKey = String.valueOf(featureMz) + String.valueOf(featureRtValue);
    }

    public int getNumberCarbons() {
        return lipid.getNumberCarbons();
    }

    public int getNumberDoubleBonds() {
        return lipid.getNumberDoubleBonds();
    }

    public String getLipidType() {
        return lipid.getLipidType();
    }

    public void addRtScore(boolean value, String featKey) {
        scores.addRtScore(value, featKey);
    }
}
