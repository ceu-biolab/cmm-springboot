package ceu.biolab.cmm.scoreAnnotations.model;

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
    private Optional<Double> adductScore;
    private Optional<Double> rtScore;

    public LipidScores() {
        this.rtScoreMap = new HashMap<>();
        this.ionizationScore = Optional.empty();
        this.adductScore = Optional.empty();
        this.rtScore = Optional.empty();
    }

    public Map<String, String> getScores() {
        Map<String, String> scores = new HashMap<>();
        scores.put("ionization", ionizationScore.isPresent() ? ionizationScore.get().toString() : "");
        scores.put("adduct", adductScore.isPresent() ? adductScore.get().toString() : "");
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
