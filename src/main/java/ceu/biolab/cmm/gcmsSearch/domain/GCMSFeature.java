package ceu.biolab.cmm.gcmsSearch.domain;

import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
public class GCMSFeature {

    private Spectrum gcmsSpectrumExperimental;

    private double RIExperimental;

    private List<GCMSAnnotation> gcmsAnnotations;

}
