package ceu.biolab.cmm.MSMSSearch.domain;

import ceu.biolab.cmm.shared.domain.compound.Compound;
import lombok.Data;

@Data
public class MSMSAnnotation {
    private int msmsId;
    private Compound compound;
    private String adduct;
    private Double deltaPpmPrecursorIon;
    private Double msmsCosineScore;
    private Double collisionEnergy;
    private Spectrum spectrum;
}
