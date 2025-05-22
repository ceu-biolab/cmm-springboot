package ceu.biolab.cmm.gcms.dto;

import ceu.biolab.cmm.gcms.domain.GCMSFeature;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class GCMSSearchResponseDTO {
    private List<GCMSFeature> gcmsFeatures;

    public GCMSSearchResponseDTO(List<GCMSFeature> gcmsFeatures) {
        this.gcmsFeatures = gcmsFeatures != null ? gcmsFeatures : new ArrayList<>();
    }

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
