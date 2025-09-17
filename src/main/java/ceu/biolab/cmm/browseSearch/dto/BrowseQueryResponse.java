package ceu.biolab.cmm.browseSearch.dto;

import ceu.biolab.cmm.shared.domain.compound.Compound;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BrowseQueryResponse {
    private List<Compound> compoundlist;
    public BrowseQueryResponse() {
        this.compoundlist = new ArrayList<>();
    }

    public BrowseQueryResponse(List<Compound> compoundlist) {
        this.compoundlist = compoundlist;
    }
}