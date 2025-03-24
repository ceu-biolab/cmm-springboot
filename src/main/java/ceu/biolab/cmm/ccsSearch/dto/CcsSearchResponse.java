package ceu.biolab.cmm.ccsSearch.dto;

import java.util.ArrayList;
import java.util.List;

import ceu.biolab.cmm.ccsSearch.domain.IMFeature;

public class CcsSearchResponse {
    private List<IMFeature> imFeatures;

    public CcsSearchResponse() {
        this.imFeatures = new ArrayList<>();
    }

    public void addImFeature(IMFeature imFeature) {
        if (imFeature != null) {
            this.imFeatures.add(imFeature);
        }
    }

    public List<IMFeature> getImFeatures() {
        return imFeatures;
    }

    public void setImFeatures(List<IMFeature> imFeatures) {
        this.imFeatures = imFeatures;
    }

    @Override
    public String toString() {
        return "CcsSearchResponse [imFeatures=" + imFeatures + "]";
    }
}
