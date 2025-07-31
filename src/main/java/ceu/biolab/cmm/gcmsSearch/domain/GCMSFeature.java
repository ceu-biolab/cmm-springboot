package ceu.biolab.cmm.gcmsSearch.domain;

import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Optional;

@Data
@SuperBuilder
public class GCMSFeature {

    //private List<Peak> gcmsSpectrum; // CHANGE FOR SPECTRUM-> done

    //private List<Spectrum> gcmsSpectrumExperimental; //TODO 1 o son varios?
    private Spectrum gcmsSpectrumExperimental;

    private double RIExperimental;
    //private Optional<Double> RT;

    private List<GCMSAnnotation> gcmsAnnotations;

    /*public GCMSFeature(List<Peak> GCMSSpectrum, double RI, Optional<Double> RT,
                       List<GCMSAnnotation> gcmsAnnotations) {
        this.GCMSSpectrum = GCMSSpectrum != null ? GCMSSpectrum : new ArrayList<>();
        this.RI = RI;
        this.RT = RT;
        this.gcmsAnnotations = gcmsAnnotations != null ? gcmsAnnotations : new ArrayList<>();
    }*/

    /*
    public int cosinefunction(List<GCMSAnnotation> gcmsAnnotations){
        int score=0;
        return score;
    }*/

}
