package ceu.biolab.cmm.scoreAnnotations.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
    private static final double MIN_RETENTION_TIME_SCORE = 0.05d;

    @JsonIgnore
    private Map<String, List<Boolean>> rtScoreMap;
    @JsonIgnore
    private int totalNumberRtScores;
    private Optional<Double> ionizationScore;
    private Optional<Double> adductRelationScore;
    private Optional<Double> rtScore;
    private Optional<Double> finalScore;

    public CompoundScores() {
        this.rtScoreMap = new HashMap<>();
        this.totalNumberRtScores = 0;
        this.ionizationScore = Optional.empty();
        this.adductRelationScore = Optional.empty();
        this.rtScore = Optional.empty();
        this.finalScore = Optional.empty();
    }

    public static String calculateFeatureKey(double featureMzValue, double featureRtValue) {
        return String.valueOf(featureMzValue) + String.valueOf(featureRtValue);
    }

    @JsonIgnore
    public Map<String, String> getScores() {
        Map<String, String> scores = new HashMap<>();
        scores.put("ionization", ionizationScore.isPresent() ? ionizationScore.get().toString() : "");
        scores.put("adduct", adductRelationScore.isPresent() ? adductRelationScore.get().toString() : "");
        scores.put("rt", rtScore.isPresent() ? rtScore.get().toString() : "");
        scores.put("final", finalScore.isPresent() ? finalScore.get().toString() : "");
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

    public void calculateRtScore() {
        if (rtScoreMap == null || rtScoreMap.isEmpty()) {
            totalNumberRtScores = 0;
            rtScore = Optional.empty();
            return;
        }

        totalNumberRtScores = rtScoreMap.size();
        double accumulatedRtScore = 0.0d;

        for (List<Boolean> relativeScores : rtScoreMap.values()) {
            if (relativeScores == null || relativeScores.isEmpty()) {
                continue;
            }

            int positives = 0;
            for (Boolean relativeScore : relativeScores) {
                if (Boolean.TRUE.equals(relativeScore)) {
                    positives++;
                }
            }

            // Mediator used integer division here, so any mixed comparison list collapsed to 0.
            accumulatedRtScore += positives / relativeScores.size();
        }

        double calculatedRtScore = accumulatedRtScore / rtScoreMap.size();
        if (calculatedRtScore < MIN_RETENTION_TIME_SCORE && calculatedRtScore >= 0.0d) {
            calculatedRtScore = MIN_RETENTION_TIME_SCORE;
        }

        rtScore = Optional.of(calculatedRtScore);
    }

    public void calculateFinalScore(int maxNumberOfRtScoresApplied) {
        double numeratorFinalScore = 0.0d;
        double denominatorFinalScore = 0.0d;

        if (ionizationScore.isPresent()) {
            numeratorFinalScore += Math.log(ionizationScore.get()) * 1.0d;
            denominatorFinalScore += 1.0d;
        }

        if (adductRelationScore.isPresent()) {
            numeratorFinalScore += Math.log(adductRelationScore.get()) * 2.0d;
            denominatorFinalScore += 2.0d;
        }

        if (rtScore.isPresent()) {
            double rtWeight = calculateRtWeight(maxNumberOfRtScoresApplied);
            if (rtWeight > 0.0d) {
                numeratorFinalScore += Math.log(rtScore.get()) * rtWeight;
                denominatorFinalScore += rtWeight;
            }
        }

        if (Math.abs(denominatorFinalScore) < 0.000001d) {
            finalScore = Optional.empty();
            return;
        }

        finalScore = Optional.of(Math.exp(numeratorFinalScore / denominatorFinalScore));
    }

    private double calculateRtWeight(int maxNumberOfRtScoresApplied) {
        if (totalNumberRtScores <= 0) {
            return 0.0d;
        }

        int thresholdMaxWC = maxNumberOfRtScoresApplied / 2;
        if (thresholdMaxWC <= 0) {
            return 2.0d;
        }

        if (totalNumberRtScores > thresholdMaxWC) {
            return 2.0d;
        }

        return 2.0d * totalNumberRtScores / (double) thresholdMaxWC;
    }

    public void setAdductRelationScore(double value) {
        this.adductRelationScore = Optional.of(value);
    }

    public Optional<Double> getAdductRelationScore() {
        return adductRelationScore;
    }

    public void setIonizationScore(double value) {
        if (value == -2.0) {
            ionizationScore = Optional.of(1.0);
        } else if (value == -3.0) {
            ionizationScore = Optional.of(0.1);
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
