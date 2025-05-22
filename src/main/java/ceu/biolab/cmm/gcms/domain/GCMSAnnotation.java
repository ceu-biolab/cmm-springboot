package ceu.biolab.cmm.gcms.domain;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.msFeature.Annotation;
import lombok.Data;

import java.util.Optional;

@Data
public class GCMSAnnotation extends Annotation {
    private GCMSCompound gcmsCompound;
    private double msmsCosineScore; // calculation of the msmsCosineScore based on the input data vs database data
    private double deltaRI;
    private Optional<Double> deltaRT;

    /*public GCMSAnnotation(Compound compound, GCMSCompound gcmsCompound,
                          double msmsCosineScore, double deltaRI, Optional<Double> deltaRT) {
        super(compound);
        this.gcmsCompound = gcmsCompound;
        this.msmsCosineScore = msmsCosineScore;
        this.deltaRI = deltaRI;
        this.deltaRT = deltaRT;
    }*/
    public GCMSAnnotation(Compound compound, GCMSCompound gcmsCompound,
                          double msmsCosineScore, double deltaRI) {
        super(compound);
        this.gcmsCompound = gcmsCompound;
        this.msmsCosineScore = msmsCosineScore;
        this.deltaRI = deltaRI;
        this.deltaRT = Optional.empty();
    }
    public GCMSAnnotation(Compound compound, GCMSCompound gcmsCompound,
                          double msmsCosineScore, double deltaRI, double deltaRT) {
        super(compound);
        this.gcmsCompound = gcmsCompound;
        this.msmsCosineScore = msmsCosineScore;
        this.deltaRI = deltaRI;
        this.deltaRT = Optional.of(deltaRT);
    }

}
