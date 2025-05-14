package ceu.biolab.cmm.rtSearch.dto;

import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
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
}
