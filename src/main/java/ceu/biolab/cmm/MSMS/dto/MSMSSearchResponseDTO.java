package ceu.biolab.cmm.MSMS.dto;

import ceu.biolab.cmm.MSMS.domain.MSMSAnotation;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MSMSSearchResponseDTO {
    private List<Compound> compoundList;
    private List<MSMSAnotation> msmsList;

    public MSMSSearchResponseDTO(List<Compound> compoundsList, List<MSMSAnotation> msmsList) {
        this.compoundList = compoundsList;
        this.msmsList=msmsList;
    }

    public MSMSSearchResponseDTO() {
        this.compoundList = new ArrayList<>();
        this.msmsList = new ArrayList<>();
    }
}
