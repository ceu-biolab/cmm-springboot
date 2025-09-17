package ceu.biolab.cmm.gcmsSearch.domain;

import ceu.biolab.cmm.shared.domain.msFeature.MSPeak;
import ceu.biolab.cmm.shared.domain.msFeature.Peak;
import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import ceu.biolab.cmm.shared.service.SpectrumScorer;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
public class GCMSAnnotation {
    private GCMSCompound gcmsCompound;
    private double gcmsCosineScore;

    private double experimentalRI;
    private double deltaRI;

    public double computeCosineScore(List<MSPeak> experimentalPeaks, SpectrumScorer scorer) {
        if (experimentalPeaks == null || experimentalPeaks.isEmpty() || scorer == null) {
            this.gcmsCosineScore = 0.0;
            return this.gcmsCosineScore;
        }

        double bestScore = 0.0;
        if (gcmsCompound != null && gcmsCompound.getGCMSSpectrum() != null) {
            for (Spectrum librarySpectrum : gcmsCompound.getGCMSSpectrum()) {
                List<MSPeak> libraryPeaks = toMsPeaks(librarySpectrum);
                if (libraryPeaks.isEmpty()) {
                    continue;
                }
                double score = scorer.cosineScore(experimentalPeaks, libraryPeaks);
                if (score > bestScore) {
                    bestScore = score;
                }
            }
        }

        this.gcmsCosineScore = bestScore;
        return bestScore;
    }

    private List<MSPeak> toMsPeaks(Spectrum spectrum) {
        List<MSPeak> converted = new ArrayList<>();
        if (spectrum == null || spectrum.getSpectrum() == null) {
            return converted;
        }
        for (Peak peak : spectrum.getSpectrum()) {
            converted.add(new MSPeak(peak.getMzValue(), peak.getIntensity()));
        }
        return converted;
    }

}
