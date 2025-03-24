package ceu.biolab.cmm.ccsSearch.domain;

import java.util.List;
import java.util.ArrayList;

public class AnnotationsByAdduct {
    private String adduct;
    private List<IMMSFeature> compounds;

    public AnnotationsByAdduct(String adduct, List<IMMSFeature> compounds) {
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

    public String getAdduct() {
        return adduct;
    }

    public void setAdduct(String adduct) {
        this.adduct = adduct;
    }

    public List<IMMSFeature> getCompounds() {
        return compounds;
    }

    public void setCompounds(List<IMMSFeature> compounds) {
        if (compounds == null) {
            this.compounds = new ArrayList<>();
        } else {
            this.compounds = new ArrayList<>(compounds);
        }
    }

    @Override
    public String toString() {
        return "AnnotationsByAdduct [adduct=" + adduct + ", compounds=" + compounds + "]";
    }
}
