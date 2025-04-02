package ceu.biolab.cmm.shared.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class AnnotationsByAdduct {
    private String adduct;
    private List<Compound> scoredCompounds;

    public AnnotationsByAdduct(String adduct) {
        this.adduct = adduct;
        this.scoredCompounds = new ArrayList<>();
    }

    public AnnotationsByAdduct(String adduct, List<Compound> scoredCompounds) {
        this.adduct = adduct;
        this.scoredCompounds = scoredCompounds;
    }

    public void addScoredCompound(Compound scoredCompound) {
        this.scoredCompounds.add(scoredCompound);
    }
}
