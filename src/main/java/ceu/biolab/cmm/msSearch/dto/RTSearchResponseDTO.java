package ceu.biolab.cmm.msSearch.dto;

import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;

import java.util.ArrayList;
import java.util.List;

public class RTSearchResponseDTO {
    private List<AnnotatedFeature> msFeatures;

    public RTSearchResponseDTO() {
        this.msFeatures = new ArrayList<>();
    }

    public void addIMSFeature(AnnotatedFeature imsFeature) {
        if (imsFeature != null) {
            this.msFeatures.add(imsFeature);
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
