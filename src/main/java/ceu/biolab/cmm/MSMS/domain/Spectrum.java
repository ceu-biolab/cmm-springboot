package ceu.biolab.cmm.MSMS.domain;

import ceu.biolab.cmm.shared.domain.msFeature.MSPeak;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Spectrum {
    private List<MSPeak> peaks;

    public Spectrum() {
        this.peaks = new ArrayList<>();
    }

    public Spectrum(List<MSPeak> peaks) {
        this.peaks = peaks;
    }
}
