package ceu.biolab.cmm.scoreAnnotations.domain;

import ceu.biolab.cmm.shared.domain.compound.Compound;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EvaluatedCompound {
    private Compound compound;
    private String featureKey;
    private double featureMz;
    private double featureRtValue;
    private String adduct;
    private CompoundScores scores;
    private boolean significative;

    public EvaluatedCompound(Compound compound, double featureMz, double featureRtValue, String adduct, CompoundScores scores) {
        this(compound, featureMz, featureRtValue, adduct, scores, true);
    }

    public EvaluatedCompound(Compound compound, double featureMz, double featureRtValue, String adduct, CompoundScores scores, boolean significative) {
        this.compound = compound;
        this.featureMz = featureMz;
        this.featureRtValue = featureRtValue;
        this.adduct = adduct;
        this.scores = scores;
        this.significative = significative;
        this.featureKey = CompoundScores.calculateFeatureKey(featureMz, featureRtValue);
    }

    public int getCompoundId() {
        return compound.getCompoundId();
    }

    public String getCompoundName() {
        return compound.getCompoundName();
    }

    public String getAdduct() {
        return adduct;
    }

    public double getFeatureRtValue() {
        return featureRtValue;
    }

    public double getFeatureMz() {
        return featureMz;
    }

    public String getFeatureKey() {
        return featureKey;
    }

    public CompoundScores getScores() {
        return scores;
    }

    public void setAdductRelationScore(double value) {
        scores.setAdductRelationScore(value);
    }

    public double getAdductRelationScore() {
        return scores.getAdductRelationScore().orElse(0.0);
    }

    public void setIonizationScore(double value) {
        scores.setIonizationScore(value);
    }

    public void addRtScore(boolean value, String featKey) {
        scores.addRtScore(value, featKey);
    }

    public void addRtScore(boolean value, double featureRtValue, double featureMzValue) {
        scores.addRtScore(value, featureRtValue, featureMzValue);
    }
}

