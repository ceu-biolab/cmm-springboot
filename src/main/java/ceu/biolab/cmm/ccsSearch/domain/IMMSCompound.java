package ceu.biolab.cmm.ccsSearch.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.compound.Pathway;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class IMMSCompound extends Compound {
    private double dbCcs;
    // TODO pathways should be on the shared domain
    private Set<Pathway> pathways;

    public IMMSCompound(Compound compound, double ccsValue) {
        super(compound.getCompoundId(), compound.getCasId(), compound.getCompoundName(), compound.getFormula(),
              compound.getMass(), compound.getChargeType(), compound.getChargeNumber(), compound.getFormulaType(),
              compound.getCompoundType(), compound.getCompoundStatus(), compound.getFormulaTypeInt(),
              compound.getLogP(), compound.getRtPred(), compound.getInchi(), compound.getInchiKey(),
              compound.getSmiles(), compound.getLipidType(), compound.getNumChains(), compound.getNumCarbons(),
              compound.getDoubleBonds(), compound.getBiologicalActivity(), compound.getMeshNomenclature(),
              compound.getIupacClassification(), compound.getMol2(), compound.getPathways());
        this.dbCcs = ccsValue;
        this.pathways = new HashSet<>();
    }

    public void addPathway(Pathway pathway) {
        if (this.pathways == null) {
            this.pathways = new HashSet<>();
        }
        if (pathway != null && pathway.getPathwayId() != -1) {
            this.pathways.add(pathway);
        }
    }
}
