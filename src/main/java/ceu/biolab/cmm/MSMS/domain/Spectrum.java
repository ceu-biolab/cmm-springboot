package ceu.biolab.cmm.MSMS.domain;

import java.util.ArrayList;
import java.util.List;

public class Spectrum {
    private List<Peak> peaks;

    public Spectrum() {
        this.peaks = new ArrayList<>();
    }
    public Spectrum(List<Peak> peaks) {
        this.peaks = peaks;
    }
    public List<Peak> getPeaks() {
        return peaks;
    }
    public void setPeaks(List<Peak> peaks) {
        this.peaks = peaks;
    }
}
