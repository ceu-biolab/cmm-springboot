package ceu.biolab.cmm.MSMS.dto;

import ceu.biolab.cmm.MSMS.domain.MSMS;
import ceu.biolab.cmm.shared.domain.compound.Compound;

import java.util.List;

public class MSMSSearchResponseDTO {
    List<Compound> compoundsList;
    List<MSMS> msmsList;

    public MSMSSearchResponseDTO(List<Compound> compoundsList,List<MSMS> msmsList) {
        this.compoundsList = compoundsList;
        this.msmsList=msmsList;
    }

    public MSMSSearchResponseDTO() {
    }

    public List<Compound> getCompoundsList() {
        return compoundsList;
    }
    public void setCompoundsList(List<Compound> compoundsList) {
        this.compoundsList = compoundsList;
    }

    public void setMsmsList(List<MSMS> msmsList) {
        this.msmsList = msmsList;
    }
}
