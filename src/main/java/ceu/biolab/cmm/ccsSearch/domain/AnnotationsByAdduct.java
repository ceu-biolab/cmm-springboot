package ceu.biolab.cmm.ccsSearch.domain;

import java.util.List;
import java.util.ArrayList;

import lombok.Data;

@Data
public class AnnotationsByAdduct {
    private String adduct;
    private List<IMMSCompound> compounds;

    public AnnotationsByAdduct(String adduct, List<IMMSCompound> compounds) {
        this.adduct = adduct;
        if (compounds != null) {
            this.compounds = compounds;
        } else {
            this.compounds = new ArrayList<>();
        }
    }

    public AnnotationsByAdduct(String adduct) {
        this.adduct = adduct;
        this.compounds = new ArrayList<>();
    }

    public void setCompounds(List<IMMSCompound> compounds) {
        if (compounds == null) {
            this.compounds = new ArrayList<>();
        } else {
            this.compounds = new ArrayList<>(compounds);
        }
    }
}
