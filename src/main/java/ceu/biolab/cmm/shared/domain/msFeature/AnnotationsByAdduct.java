package ceu.biolab.cmm.shared.domain.msFeature;

import java.util.ArrayList;
import java.util.List;

import ceu.biolab.cmm.shared.domain.compound.Compound;
import lombok.Data;

@Data
public class AnnotationsByAdduct {
    private String adduct;
    private List<Annotation> annotations;

    // no-arg constructor for Jackson
    public AnnotationsByAdduct() {
        this.annotations = new ArrayList<>();
    }

    public AnnotationsByAdduct(String adduct) {
        this.adduct = adduct;
        this.annotations = new ArrayList<>();
    }

    public AnnotationsByAdduct(String adduct, List<Annotation> annotatedCompounds) {
        this.adduct = adduct;
        this.annotations = annotatedCompounds;
    }

    public void addAnnotation(Annotation annotation) {
        this.annotations.add(annotation);
    }

    public void addUnannotatedCompound(Compound compound) {
        Annotation annotation = new Annotation(compound);
        this.annotations.add(annotation);
    }
}
