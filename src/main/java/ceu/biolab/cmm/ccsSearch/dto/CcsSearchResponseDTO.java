package ceu.biolab.cmm.ccsSearch.dto;

import java.util.ArrayList;
import java.util.List;

import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;

public class CcsSearchResponseDTO {
    private List<AnnotatedFeature> imFeatures;

    public CcsSearchResponseDTO() {
        this.imFeatures = new ArrayList<>();
    }

    public void addImFeature(AnnotatedFeature imFeature) {
        if (imFeature != null) {
            this.imFeatures.add(imFeature);
        }
    }

    public List<AnnotatedFeature> getImFeatures() {
        return imFeatures;
    }

    public void setImFeatures(List<AnnotatedFeature> imFeatures) {
        this.imFeatures = imFeatures;
    }

    @Override
    public String toString() {
        return "CcsSearchResponse [imFeatures=" + imFeatures + "]";
    }
}
