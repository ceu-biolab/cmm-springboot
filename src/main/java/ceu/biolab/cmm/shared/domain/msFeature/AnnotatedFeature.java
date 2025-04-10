package ceu.biolab.cmm.shared.domain.msFeature;

import java.util.*;

import ceu.biolab.cmm.shared.domain.compound.Compound;
import lombok.Data;

@Data
public class AnnotatedFeature {
    private IMSFeature feature;
    private List<AnnotationsByAdduct> annotationsByAdducts;

    public AnnotatedFeature(double mzValue) {
        this.feature = new MSFeature(mzValue);
        this.annotationsByAdducts = new ArrayList<>();
    }

    public AnnotatedFeature(double mzValue, double rtValue) {
        this.feature = new LCMSFeature(mzValue, rtValue);
        this.annotationsByAdducts = new ArrayList<>();
    }

    public AnnotatedFeature(IMSFeature feature) {
        this.feature = feature;
        this.annotationsByAdducts = new ArrayList<>();
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
            annotationsByAdduct.get().addUnannotatedCompound(compound);
        } else {
            AnnotationsByAdduct newAnnotationsByAdduct = new AnnotationsByAdduct(adduct);
            newAnnotationsByAdduct.addUnannotatedCompound(compound);
            this.annotationsByAdducts.add(newAnnotationsByAdduct);
        }
    }
}
