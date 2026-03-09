package ceu.biolab.cmm.MSMSSearch.domain;

import ceu.biolab.cmm.shared.domain.msFeature.MSPeak;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Spectrum {
    private Double precursorMz;
    @NotEmpty
    private List<@Valid MSPeak> peaks;
    public Spectrum() {
        this.peaks = new ArrayList<>();
    }
    public Spectrum(Double precursorMz, List<MSPeak> peaks) {
        this.precursorMz = precursorMz;
        this.peaks = peaks;
    }
}
