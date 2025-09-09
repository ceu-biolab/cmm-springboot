package ceu.biolab.cmm.shared.domain.msFeature;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
public class Spectrum {
    private List<Peak> spectrum; //Peak has mz & intensity
    private int spectrumId;

    public Spectrum() {
        this.spectrum = new ArrayList<>();
        this.spectrumId = -1;
    }

}
