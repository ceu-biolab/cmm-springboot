package ceu.biolab.cmm.shared.domain.msFeature;

import java.util.ArrayList;
import java.util.List;

import ceu.biolab.cmm.shared.domain.compound.Compound;
import lombok.Data;

@Data
public class Annotation {
    private Compound compound;
    private List<IScore> scores;

    public Annotation(Compound compound) {
        this.compound = compound;
        this.scores = new ArrayList<>();
    }

    public void addScore(IScore score) {
        this.scores.add(score);
    }
}
