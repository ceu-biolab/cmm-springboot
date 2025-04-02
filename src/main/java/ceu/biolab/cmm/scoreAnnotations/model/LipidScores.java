package ceu.biolab.cmm.scoreAnnotations.model;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import lombok.Data;

@Data
public class LipidScores {
    private Map<String, List<Boolean>> rtScoreMap;
    private double ionizationScore;
    private double adductScore;
    private double rtScore;

    public LipidScores() {
        this.rtScoreMap = new HashMap<>();
        this.ionizationScore = 0.0;
        this.adductScore = 0.0;
        this.rtScore = 0.0;
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
        String featKey = String.valueOf(featureMzValue) + String.valueOf(featureRtValue);
        if (getRtScoreMap() == null) {
            setRtScoreMap(new HashMap<>());
        }
        if (!getRtScoreMap().containsKey(featKey)) {
            getRtScoreMap().put(featKey, new ArrayList<>());
        }
        rtScoreMap.get(featKey).add(value);
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
        String featureKey = String.valueOf(featureMzValue) + String.valueOf(featureRtValue);
        if (rtScoreMap == null) {
            return Optional.empty();
        }
        if (rtScoreMap.containsKey(featureKey)) {
            return Optional.of(rtScoreMap.get(featureKey));
        }
        return Optional.empty();
    }
}
