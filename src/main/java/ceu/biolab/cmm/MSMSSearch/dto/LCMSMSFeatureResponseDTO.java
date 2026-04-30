package ceu.biolab.cmm.MSMSSearch.dto;

import ceu.biolab.cmm.MSMSSearch.domain.MSMSAnnotation;
import ceu.biolab.cmm.MSMSSearch.domain.Spectrum;
import ceu.biolab.cmm.shared.domain.msFeature.LCMSFeature;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class LCMSMSFeatureResponseDTO {
    private LCMSFeature feature;
    private List<MSMSAnnotation> msmsList = new ArrayList<>();
    private Spectrum experimentalSpectrum;
}
