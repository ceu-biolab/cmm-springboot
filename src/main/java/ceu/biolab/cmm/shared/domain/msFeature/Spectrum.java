package ceu.biolab.cmm.shared.domain.msFeature;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
public class Spectrum /*implements IMSFeature*/ {
    private List<Peak> spectrum; //Peak has mz & intensity
    private int spectrumId;
    //private int compoundId;

    public Spectrum() {
        this.spectrum = new ArrayList<>();
        this.spectrumId = -1; //it is a non valid value -> an id cannot be negative
        //this.compoundId = -1;
    }

}
