package ceu.biolab.cmm.ccsSearch.dto;

import java.util.ArrayList;
import java.util.List;

import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import lombok.Data;

@Data
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

    @Override
    public String toString() {
        return "CcsSearchResponse [imFeatures=" + imFeatures + "]";
    }
}
