package ceu.biolab.cmm.MSMS.domain;

import ceu.biolab.cmm.shared.domain.msFeature.MSPeak;

import java.util.ArrayList;
import java.util.List;

public class Spectrum {
    private List<MSPeak> peaks;

    public Spectrum() {
        this.peaks = new ArrayList<>();
    }

    public Spectrum(List<MSPeak> peaks) {
        this.peaks = peaks;
    }

    public List<MSPeak> getPeaks() {
        return peaks;
    }

    public void setPeaks(List<MSPeak> peaks) {
        this.peaks = peaks;
    }
}
