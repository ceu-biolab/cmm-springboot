package com.example.myapp.model.compound;

import java.util.Set;
import java.util.TreeSet;

public class GroupedCompoundsByAdduct {
    private String adduct;
    private Set<CMMCompound> cmm_compounds;

    public GroupedCompoundsByAdduct(String adduct) {
        this.adduct = adduct;
        this.cmm_compounds = new TreeSet<>();
    }

    public String getAdduct() {
        return this.adduct;
    }

    public void setAdduct(String adduct) {
        this.adduct = adduct;
    }

    public Set<CMMCompound> getCmm_compounds() {
        return this.cmm_compounds;
    }

    public void setCmm_compounds(Set<CMMCompound> cmm_compounds) {
        this.cmm_compounds = cmm_compounds;
    }

    @Override
    public String toString() {
        return "GroupedCompoundsByAdduct{" +
                "adduct='" + this.adduct + '\'' + ", cmm_compounds=" + this.cmm_compounds + '}';
    }
}
