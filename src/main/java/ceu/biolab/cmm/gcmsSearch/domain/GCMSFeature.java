package ceu.biolab.cmm.gcmsSearch.domain;

import ceu.biolab.cmm.shared.domain.msFeature.Peak;
import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Optional;

@Data
@SuperBuilder
public class GCMSFeature {

    //private List<Peak> gcmsSpectrum; // CHANGE FOR SPECTRUM-> done
    /*TODO ?????
    private List<Spectrum> gcmsSpectrum;
    private double RI;
    private Optional<Double> RT;*/

    private List<GCMSAnnotation> gcmsAnnotations;

    /*public GCMSFeature(List<Peak> GCMSSpectrum, double RI, Optional<Double> RT,
                       List<GCMSAnnotation> gcmsAnnotations) {
        this.GCMSSpectrum = GCMSSpectrum != null ? GCMSSpectrum : new ArrayList<>();
        this.RI = RI;
        this.RT = RT;
        this.gcmsAnnotations = gcmsAnnotations != null ? gcmsAnnotations : new ArrayList<>();
    }*/

    /*//todo -> gcmsAnotation
    public int cosinefunction(List<GCMSAnnotation> gcmsAnnotations){
        int score=0;
        return score;
    }*/

}
