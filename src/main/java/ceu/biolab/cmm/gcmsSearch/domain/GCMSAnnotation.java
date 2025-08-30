package ceu.biolab.cmm.gcmsSearch.domain;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.msFeature.Annotation;
import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Optional;

@Data
@SuperBuilder
public class GCMSAnnotation {
    private GCMSCompound gcmsCompound;
    //private GCMSCompoundAll gcmsCompoundAll;
    private double gcmsCosineScore; // calculation of the msmsCosineScore based on the input data vs database data

    private double experimentalRI; // is the one that the user use
    private double deltaRI; //experimental - dbRI
    //private Optional<Double> experimentalRT;
    //private Optional<Double> deltaRT;

    /*public GCMSAnnotation(Compound compound, GCMSCompound gcmsCompound,
                          double msmsCosineScore, double deltaRI, Optional<Double> deltaRT) {
        super(compound);
        this.gcmsCompound = gcmsCompound;
        this.msmsCosineScore = msmsCosineScore;
        this.deltaRI = deltaRI;
        this.deltaRT = deltaRT;
    }*/
    /*public GCMSAnnotation(Compound compound, GCMSCompound gcmsCompound,
                          double gcmsCosineScore, double deltaRI) {
        super(compound);
        this.gcmsCompound = gcmsCompound;
        this.gcmsCosineScore = gcmsCosineScore;
        this.deltaRI = deltaRI;
        this.deltaRT = Optional.empty();
    }*/
    /*public GCMSAnnotation(Compound compound, GCMSCompound gcmsCompound,
                          double gcmsCosineScore, double deltaRI, double deltaRT) {
        super(compound);
        this.gcmsCompound = gcmsCompound;
        this.gcmsCosineScore = gcmsCosineScore;
        this.deltaRI = deltaRI;
        this.deltaRT = Optional.of(deltaRT);
    }*/

    //TODO not the correct method
    public void cosineScoreFunction(Spectrum gcmsSpectrumExperimental){
        int score=0;
        int sizeListgcmsCompundSpectrum = this.gcmsCompound.getGCMSSpectrum().size();
        for (int i=0; i<sizeListgcmsCompundSpectrum; i++){
            Spectrum gcmsSpectrumCompound = this.gcmsCompound.getGCMSSpectrum().get(i);
        }
        //return score;
        this.gcmsCosineScore = score;
    }

}
