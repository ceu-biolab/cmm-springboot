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
    private String adduct;
    private LipidScores scores;
    private boolean isSignificative;

    public EvaluatedLipid(Lipid lipid, double featureMz, double featureRtValue, String adduct, LipidScores scores, boolean isSignificative) {
        this.lipid = lipid;
        this.featureMz = featureMz;
        this.featureRtValue = featureRtValue;
        this.adduct = adduct;
        this.scores = scores;
        this.isSignificative = isSignificative;

        this.featureKey = LipidScores.calculateFeatureKey(featureMz, featureRtValue);
    }

    public EvaluatedLipid(Lipid lipid, double featureMz, double featureRtValue, String adduct, LipidScores scores) {
        this.lipid = lipid;
        this.featureMz = featureMz;
        this.featureRtValue = featureRtValue;
        this.adduct = adduct;
        this.scores = scores;
        this.isSignificative = true;
        
        this.featureKey = LipidScores.calculateFeatureKey(featureMz, featureRtValue);
    }

    public void setAdductRelationScore(double value) {
        scores.setAdductRelationScore(value);
    }

    public void setIonizationScore(double value) {
        scores.setIonizationScore(value);
    }

    public double getFeatureRtValue() {
        return featureRtValue;
    }
    
    public int getCompoundId() {
        return lipid.getCompoundId();
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
