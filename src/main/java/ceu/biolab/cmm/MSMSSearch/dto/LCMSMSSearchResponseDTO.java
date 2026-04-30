package ceu.biolab.cmm.MSMSSearch.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import ceu.biolab.cmm.MSMSSearch.domain.MSMSAnnotation;
import ceu.biolab.cmm.MSMSSearch.domain.Spectrum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LCMSMSSearchResponseDTO {
    private List<LCMSMSFeatureResponseDTO> msmsFeatures = new ArrayList<>();
    private List<MSMSAnnotation> msmsList;
    private Spectrum experimentalSpectrum;

    public void addMsmsFeature(LCMSMSFeatureResponseDTO feature) {
        if (feature != null) {
            msmsFeatures.add(feature);
        }
    }
}
