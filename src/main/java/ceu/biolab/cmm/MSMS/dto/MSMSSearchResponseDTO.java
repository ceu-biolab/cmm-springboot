package ceu.biolab.cmm.MSMS.dto;

import ceu.biolab.cmm.MSMS.domain.MSMSAnotation;
import ceu.biolab.cmm.shared.domain.compound.Compound;

import java.util.List;

public class MSMSSearchResponseDTO { ;
    List<MSMSAnotation> msmsList;

    public MSMSSearchResponseDTO(List<Compound> compoundsList,List<MSMSAnotation> msmsList) {

        this.msmsList=msmsList;
    }

    public MSMSSearchResponseDTO() {
    }

    public void setMsmsList(List<MSMSAnotation> msmsList) {
        this.msmsList = msmsList;
    }

    public List<MSMSAnotation> getMsmsList() {
        return msmsList;
    }
}
