package ceu.biolab.cmm.rtSearch.dto;

import ceu.biolab.cmm.ccsSearch.domain.IMFeature;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;

import java.util.ArrayList;
import java.util.List;

public class RtSearchResponse {
    private List<AnnotatedFeature> imFeatures;

    public RtSearchResponse() {
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
