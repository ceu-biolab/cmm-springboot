package ceu.biolab.cmm.ccsSearch.domain;

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

    public IMMSCompound(Compound compound, double ccsValue) {
        super(compound.getCompoundId(), compound.getCasId(), compound.getCompoundName(), compound.getFormula(),
              compound.getMass(), compound.getChargeType(), compound.getChargeNumber(), compound.getFormulaType(),
              compound.getCompoundType(),
              compound.getLogP(), compound.getRtPred(), compound.getInchi(), compound.getInchiKey(),
              compound.getSmiles(), compound.getLipidType(), compound.getNumChains(), compound.getNumCarbons(),
              compound.getDoubleBonds(), compound.getBiologicalActivity(), compound.getMeshNomenclature(),
              compound.getIupacClassification(), compound.getMol2(), compound.getPathways());
        this.dbCcs = ccsValue;
    }

    public void addPathway(Pathway pathway) {
        if (pathway == null || pathway.getPathwayId() == -1) {
            return;
        }
        if (getPathways() == null) {
            setPathways(new java.util.LinkedHashSet<>());
        }
        getPathways().add(pathway);
    }
}
