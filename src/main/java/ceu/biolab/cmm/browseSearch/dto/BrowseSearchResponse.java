package ceu.biolab.cmm.browseSearch.dto;

import ceu.biolab.cmm.shared.domain.compound.Compound;

import java.util.ArrayList;
import java.util.List;

public class BrowseSearchResponse {
    private List<Compound> compoundList;

    public BrowseSearchResponse(List<Compound> compoundList) {
        this.compoundList = compoundList;
    }
    public BrowseSearchResponse() {
        this.compoundList = new ArrayList<>();
    }

    public List<Compound> getCompoundList() {return compoundList;}

    public void setCompoundList(List<Compound> compoundList) {
        this.compoundList = compoundList;}

    public void addCompound(Compound compound) {
        if(compound!=null) this.compoundList.add(compound);}

    @Override
    public String toString() {
        return "BrowseSearchResponse{" +
                "compoundList=" + compoundList +
                '}';
    }
}
