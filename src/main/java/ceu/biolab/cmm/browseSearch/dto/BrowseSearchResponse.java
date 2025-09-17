package ceu.biolab.cmm.browseSearch.dto;

import ceu.biolab.cmm.shared.domain.compound.Compound;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BrowseSearchResponse {
    private List<Compound> compoundList;

    public BrowseSearchResponse(List<Compound> compoundList) {
        this.compoundList = compoundList;
    }
    public BrowseSearchResponse() {
        this.compoundList = new ArrayList<>();
    }

    public void addCompound(Compound compound) {
        if(compound!=null) {
            this.compoundList.add(compound);
        }
    }

}
