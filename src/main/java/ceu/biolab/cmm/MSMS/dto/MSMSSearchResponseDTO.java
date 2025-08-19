package ceu.biolab.cmm.MSMS.dto;

import ceu.biolab.cmm.MSMS.domain.MSMSAnotation;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MSMSSearchResponseDTO {
    private List<MSMSAnotation> msmsList;

}
