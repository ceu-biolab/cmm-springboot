package ceu.biolab.cmm.CEMSSearch.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CemsSearchResponseDTO {

    @JsonProperty("ceFeatures")
    private List<CeFeatureAnnotationsDTO> ceFeatures;

    public CemsSearchResponseDTO() {
        this.ceFeatures = new ArrayList<>();
    }

    public void addFeature(CeFeatureAnnotationsDTO featureAnnotations) {
        if (featureAnnotations != null) {
            this.ceFeatures.add(featureAnnotations);
        }
    }
}
