package ceu.biolab.cmm.scoreAnnotations.domain;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import ceu.biolab.cmm.shared.domain.msFeature.IScore;
import lombok.Data;

@Data
public class LipidScores implements IScore{
    private Map<String, List<Boolean>> rtScoreMap;
    private Optional<Double> ionizationScore;
    private Optional<Double> adductRelationScore;
    private Optional<Double> rtScore;

    public LipidScores() {
        this.rtScoreMap = new HashMap<>();
        this.ionizationScore = Optional.empty();
        this.adductRelationScore = Optional.empty();
        this.rtScore = Optional.empty();
    }

    public static String calculateFeatureKey(double featureMzValue, double featureRtValue) {
        return String.valueOf(featureMzValue) + String.valueOf(featureRtValue);
    }

    public Map<String, String> getScores() {
        Map<String, String> scores = new HashMap<>();
        scores.put("ionization", ionizationScore.isPresent() ? ionizationScore.get().toString() : "");
        scores.put("adduct", adductRelationScore.isPresent() ? adductRelationScore.get().toString() : "");
        scores.put("rt", rtScore.isPresent() ? rtScore.get().toString() : "");
        
        return scores;
    }

    public void addRtScore(boolean value, String featKey) {
        if (getRtScoreMap() == null) {
            setRtScoreMap(new HashMap<>());
        }
        if (!getRtScoreMap().containsKey(featKey)) {
            getRtScoreMap().put(featKey, new ArrayList<>());
        }
        rtScoreMap.get(featKey).add(value);
    }

    public void addRtScore(boolean value, double featureRtValue, double featureMzValue) {
        String featKey = LipidScores.calculateFeatureKey(featureMzValue, featureRtValue);
        if (getRtScoreMap() == null) {
            setRtScoreMap(new HashMap<>());
        }
        if (!getRtScoreMap().containsKey(featKey)) {
            getRtScoreMap().put(featKey, new ArrayList<>());
        }
        rtScoreMap.get(featKey).add(value);
    }

    public void setAdductRelationScore(double value) {
        this.adductRelationScore = Optional.of(value);
    }

    public void setIonizationScore(double value) {
        if (this.ionizationScore.isEmpty()) {
            this.ionizationScore = Optional.of(0.0);
        }
        
        if (value == -2.0) {
            if (this.ionizationScore.get() != -1.0) {
                this.ionizationScore = Optional.of(1.0);
            }
        } else if (value == -3.0) {
            if (this.ionizationScore.get() != -1.0) {
                this.ionizationScore = Optional.of(0.1);
            }
        } else {
            this.ionizationScore = Optional.of(value);
        }
    }

    public Optional<List<Boolean>> getRtScoresComparedTo(String featKey) {
        if (rtScoreMap == null) {
            return Optional.empty();
        }
        if (rtScoreMap.containsKey(featKey)) {
            return Optional.of(rtScoreMap.get(featKey));
        }
        return Optional.empty();
    }

    public Optional<List<Boolean>> getRtScoresComparedTo(double featureRtValue, double featureMzValue) {
        String featureKey = LipidScores.calculateFeatureKey(featureMzValue, featureRtValue);
        if (rtScoreMap == null) {
            return Optional.empty();
        }
        if (rtScoreMap.containsKey(featureKey)) {
            return Optional.of(rtScoreMap.get(featureKey));
        }
        return Optional.empty();
    }
}
