package ceu.biolab.cmm.CEMSSearch.dto;

import ceu.biolab.cmm.shared.domain.compound.Compound;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CeAnnotationDTO {
    Compound compound;
    Double score;
    Integer rank;
    Double massErrorPpm;
    Double mzCalc;
    Double neutralMassCalc;
    Double mobilityErrorPct;
}
