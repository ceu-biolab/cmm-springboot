package ceu.biolab.cmm.shared.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.Data;

@Data
public class AnnotatedRTFeature {
    private double rtValue;
    private double mzValue;
    private List<AnnotationsByAdduct> annotationsByAdducts;

    public AnnotatedRTFeature(double rtValue, double mzValue) {
        this.rtValue = rtValue;
        this.mzValue = mzValue;
        this.annotationsByAdducts = new ArrayList<>();
    }

    public AnnotatedRTFeature(double rtValue, double mzValue, List<AnnotationsByAdduct> annotationsByAdducts) {
        this.rtValue = rtValue;
        this.mzValue = mzValue;
        this.annotationsByAdducts = annotationsByAdducts;
    }

    public Optional<AnnotationsByAdduct> findAnnotationByAdduct(String adduct) {
        for (AnnotationsByAdduct annotationsByAdduct : annotationsByAdducts) {
            if (annotationsByAdduct.getAdduct().equals(adduct)) {
                return Optional.of(annotationsByAdduct);
            }
        }
        return Optional.empty();
    }

    public void addAnnotationByAdduct(AnnotationsByAdduct annotationsByAdduct) {
        this.annotationsByAdducts.add(annotationsByAdduct);
    }

    public void addCompoundForAdduct(String adduct, Compound compound) {
        Optional<AnnotationsByAdduct> annotationsByAdduct = findAnnotationByAdduct(adduct);
        if (annotationsByAdduct.isPresent()) {
            annotationsByAdduct.get().addScoredCompound(compound);
        } else {
            AnnotationsByAdduct newAnnotationsByAdduct = new AnnotationsByAdduct(adduct);
            newAnnotationsByAdduct.addScoredCompound(compound);
            this.annotationsByAdducts.add(newAnnotationsByAdduct);
        }
    }
}
