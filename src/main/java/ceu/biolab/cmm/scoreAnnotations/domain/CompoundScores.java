package ceu.biolab.cmm.scoreAnnotations.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ceu.biolab.cmm.shared.domain.msFeature.IScore;
import lombok.Data;

/**
 * Generic scoring container applied to any evaluated compound.
 */
@Data
public class CompoundScores implements IScore {
    private Map<String, List<Boolean>> rtScoreMap;
    private Optional<Double> ionizationScore;
    private Optional<Double> adductRelationScore;
    private Optional<Double> rtScore;

    public CompoundScores() {
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
        if (rtScoreMap == null) {
            rtScoreMap = new HashMap<>();
        }
        rtScoreMap.computeIfAbsent(featKey, _ -> new ArrayList<>()).add(value);
    }

    public void addRtScore(boolean value, double featureRtValue, double featureMzValue) {
        addRtScore(value, calculateFeatureKey(featureMzValue, featureRtValue));
    }

    public void setAdductRelationScore(double value) {
        this.adductRelationScore = Optional.of(value);
    }

    public Optional<Double> getAdductRelationScore() {
        return adductRelationScore;
    }

    public void setIonizationScore(double value) {
        if (ionizationScore.isEmpty()) {
            ionizationScore = Optional.of(0.0);
        }

        if (value == -2.0) {
            if (ionizationScore.get() != -1.0) {
                ionizationScore = Optional.of(1.0);
            }
        } else if (value == -3.0) {
            if (ionizationScore.get() != -1.0) {
                ionizationScore = Optional.of(0.1);
            }
        } else {
            ionizationScore = Optional.of(value);
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
        return getRtScoresComparedTo(calculateFeatureKey(featureMzValue, featureRtValue));
    }
}

