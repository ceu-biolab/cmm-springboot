package ceu.biolab.cmm.scoreAnnotations.dto;

import java.util.List;
import java.util.Optional;

import ceu.biolab.cmm.scoreAnnotations.model.LipidScores;
import ceu.biolab.cmm.shared.domain.Compound;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ScoredCompound {
    private Compound compound;
    private Optional<LipidScores> scores;

    public ScoredCompound() {
        this.scores = Optional.empty();
    }

    public ScoredCompound(Compound compound) {
        this.compound = compound;
        this.scores = Optional.empty();
    }

    public void setScores(LipidScores scores) {
        this.scores = Optional.of(scores);
    }

    public Optional<List<Boolean>> getRtScoresComparedTo(String featureKey) {
        if (scores.isPresent()) {
            return scores.get().getRtScoresComparedTo(featureKey);
        }
        return Optional.empty();
    }

    public Optional<List<Boolean>> getRtScoresComparedTo(double featureRtValue, double featureMzValue) {
        if (scores.isPresent()) {
            return scores.get().getRtScoresComparedTo(featureRtValue, featureMzValue);
        }
        return Optional.empty();
    }
}
