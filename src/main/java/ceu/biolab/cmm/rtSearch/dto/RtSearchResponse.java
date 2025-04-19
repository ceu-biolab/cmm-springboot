package ceu.biolab.cmm.rtSearch.dto;

import ceu.biolab.cmm.ccsSearch.domain.IMFeature;

import java.util.ArrayList;
import java.util.List;

public class RtSearchResponse {
    private List<IMFeature> imFeatures;

    public RtSearchResponse() {
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
