package ceu.biolab.cmm.gcmsSearch.domain;

import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class GCMSAnnotation {
    private GCMSCompound gcmsCompound;
    private double gcmsCosineScore;

    private double experimentalRI;
    private double deltaRI;

    /**
     * Currently this method returns a score of 0.
     * In the future it should return the highest score of the spectra belonging to an annotation.
     * The score is the similarity between the experimental spectrum and the spectrum of the annotated compound
     * @param gcmsSpectrumExperimental
     */
    public void cosineScoreFunction(Spectrum gcmsSpectrumExperimental){
        int score=0;
        int sizeListgcmsCompundSpectrum = this.gcmsCompound.getGCMSSpectrum().size();
        for (int i=0; i<sizeListgcmsCompundSpectrum; i++){
            Spectrum gcmsSpectrumCompound = this.gcmsCompound.getGCMSSpectrum().get(i);
            score = 0; // score = modifiedCosine(gcmsSpectrumExperimental, gcmsSpectrumCompound);
        }
        this.gcmsCosineScore = score;
    }

}
