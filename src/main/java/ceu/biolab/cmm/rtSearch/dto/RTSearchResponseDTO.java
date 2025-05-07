package ceu.biolab.cmm.rtSearch.dto;

import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;

import java.util.ArrayList;
import java.util.List;

public class RTSearchResponseDTO {
    private List<AnnotatedFeature> msFeatures;

    public RTSearchResponseDTO() {
        this.msFeatures = new ArrayList<>();
    }

    public void addImFeature(AnnotatedFeature imFeature) {
        if (imFeature != null) {
            this.msFeatures.add(imFeature);
        }
    }

    public List<AnnotatedFeature> getMSFeatures() {
        return msFeatures;
    }

    public void setmsFeatures(List<AnnotatedFeature> msFeatures) {
        this.msFeatures = msFeatures;
    }

    @Override
    public String toString() {
        return "RTSearchResponse [Features=" + msFeatures + "]";
    }
}
