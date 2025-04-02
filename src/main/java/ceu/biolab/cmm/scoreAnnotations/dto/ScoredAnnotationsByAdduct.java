package ceu.biolab.cmm.scoreAnnotations.dto;

import java.util.ArrayList;
import java.util.List;

import ceu.biolab.cmm.shared.domain.AnnotationsByAdduct;
import lombok.Data;

@Data
public class ScoredAnnotationsByAdduct {
    private String adduct;
    private List<ScoredCompound> annotations;

    public ScoredAnnotationsByAdduct() {
        this.adduct = "";
        this.annotations = new ArrayList<>();
    }

    public ScoredAnnotationsByAdduct(AnnotationsByAdduct annotationsByAdduct) {
        this.adduct = annotationsByAdduct.getAdduct();
        this.annotations = annotationsByAdduct.getScoredCompounds().stream()
                .map(scoredCompound -> new ScoredCompound(scoredCompound))
                .toList();
    }
}
