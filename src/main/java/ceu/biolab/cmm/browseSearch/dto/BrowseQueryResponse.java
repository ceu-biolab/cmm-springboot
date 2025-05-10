package ceu.biolab.cmm.browseSearch.dto;

import ceu.biolab.cmm.shared.domain.compound.Compound;

import java.util.ArrayList;
import java.util.List;

public class BrowseQueryResponse {
    private List<Compound> compoundlist;

    public BrowseQueryResponse(List<Compound> compoundlist) {
        this.compoundlist = compoundlist;
    }

    public BrowseQueryResponse() {
        this.compoundlist = new ArrayList<>();
    }

    public List<Compound> getCompoundlist() {
        return compoundlist;
    }

    public void setCompoundlist(List<Compound> compoundlist) {
        this.compoundlist = compoundlist;
    }

    @Override
    public String toString() {
        return "BrowseQueryResponse{" +
                "compoundlist=" + compoundlist +
                '}';
    }
}