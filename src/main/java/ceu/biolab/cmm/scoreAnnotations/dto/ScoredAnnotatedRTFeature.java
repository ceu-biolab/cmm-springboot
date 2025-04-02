package ceu.biolab.cmm.scoreAnnotations.dto;

import java.util.List;

import ceu.biolab.cmm.shared.domain.AnnotatedRTFeature;
import lombok.Data;

@Data
public class ScoredAnnotatedRTFeature {
    private double mzValue;
    private double rtValue;
    private List<ScoredAnnotationsByAdduct> scoredAnnotationsByAdducts;

    public ScoredAnnotatedRTFeature() {
        this.mzValue = 0.0;
        this.rtValue = 0.0;
        this.scoredAnnotationsByAdducts = List.of();
    }

    public ScoredAnnotatedRTFeature(AnnotatedRTFeature annotatedRTFeature) {
        this.mzValue = annotatedRTFeature.getMzValue();
        this.rtValue = annotatedRTFeature.getRtValue();
        this.scoredAnnotationsByAdducts = annotatedRTFeature.getAnnotationsByAdducts().stream()
                .map(annotationsByAdduct -> new ScoredAnnotationsByAdduct(annotationsByAdduct))
                .toList();
    }
}
