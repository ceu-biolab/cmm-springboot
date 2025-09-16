package ceu.biolab.cmm.gcmsSearch.dto;

import ceu.biolab.cmm.gcmsSearch.domain.GCMSFeature;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GCMSSearchResponseDTO {
    private List<GCMSFeature> gcmsFeatures;

    public GCMSSearchResponseDTO() {
        this.gcmsFeatures = new ArrayList<>();
    }

    public GCMSSearchResponseDTO(List<GCMSFeature> gcmsFeatures) {
        this.gcmsFeatures = gcmsFeatures != null ? gcmsFeatures : new ArrayList<>();
    }

    /**
     * Adds the new GCMSFeatures
     * @param gcmsFeature
     */
    public void addGcmsFeatures(GCMSFeature gcmsFeature) {
        if (gcmsFeature != null) {
            this.gcmsFeatures.add(gcmsFeature);
        }
    }

    public void setGcmsFeatures(List<GCMSFeature> gcmsFeatures) {
        this.gcmsFeatures = gcmsFeatures != null ? gcmsFeatures : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "GcmsSearchResponseDTO{" +
                "gcmsFeatures=" + gcmsFeatures +
                '}';
    }
}
