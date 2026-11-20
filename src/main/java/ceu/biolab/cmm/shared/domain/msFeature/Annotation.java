package ceu.biolab.cmm.shared.domain.msFeature;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import ceu.biolab.cmm.shared.domain.compound.Compound;
import lombok.Data;

@Data
public class Annotation {
    @NotNull
    @Valid
    private Compound compound;
    @NotNull
    private List<@NotNull IScore> scores;
    private Double massErrorPpm;

    // no-arg constructor for Jackson
    public Annotation() {
        this.scores = new ArrayList<>();
    }

    public Annotation(Compound compound) {
        this.compound = compound;
        this.scores = new ArrayList<>();
    }

    public void addScore(IScore score) {
        this.scores.add(score);
    }
}
