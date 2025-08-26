package ceu.biolab.cmm.MSMS.domain;

import ceu.biolab.Adduct;
import ceu.biolab.cmm.MSMS.service.SpectrumScorer;
import ceu.biolab.cmm.shared.domain.compound.CMMCompound;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import lombok.Data;

@Data
public class MSMSAnotation extends Compound implements Comparable<MSMSAnotation> {
    private int msmsID;
    private Compound compound;
    private Adduct adduct;
    private Double deltaPPMPrecursorIon;
    private Double MSMSCosineScore;
    private Spectrum spectrum;


    @Override
    public int compareTo(MSMSAnotation other) {
        int scoreComparison = Double.compare(
                other.getMSMSCosineScore() != null ? other.getMSMSCosineScore() : 0.0,
                this.MSMSCosineScore != null ? this.MSMSCosineScore : 0.0
        );

        if (scoreComparison != 0) {
            return scoreComparison;
        }

        return Double.compare(
                this.deltaPPMPrecursorIon != null ? this.deltaPPMPrecursorIon : Double.MAX_VALUE,
                other.getDeltaPPMPrecursorIon() != null ? other.getDeltaPPMPrecursorIon() : Double.MAX_VALUE
        );
    }
}
