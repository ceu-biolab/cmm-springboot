package ceu.biolab.cmm.ccsSearch.domain;

import ceu.biolab.cmm.shared.domain.compound.CMMCompound;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.compound.Pathway;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class IMMSCompound extends CMMCompound {
    private double dbCcs;

    public IMMSCompound(Compound compound, double ccsValue) {
        CMMCompound cmmCompound = compound instanceof CMMCompound ? (CMMCompound) compound : null;
        super(compound.getCompoundId(), compound.getCasId(), compound.getCompoundName(), compound.getFormula(),
              compound.getMass(), compound.getChargeType(), compound.getChargeNumber(), compound.getFormulaType(),
              compound.getCompoundType(),
              compound.getLogP(), compound.getRtPred(), compound.getInchi(), compound.getInchiKey(),
              compound.getSmiles(), compound.getLipidType(), compound.getNumChains(), compound.getNumCarbons(),
              compound.getDoubleBonds(), compound.getBiologicalActivity(), compound.getMeshNomenclature(),
              compound.getIupacClassification(), compound.getMol2(), compound.getPathways(),
              cmmCompound != null ? cmmCompound.getKeggID() : null,
              cmmCompound != null ? cmmCompound.getLmID() : null,
              cmmCompound != null ? cmmCompound.getHmdbID() : null,
              cmmCompound != null ? cmmCompound.getAgilentID() : null,
              cmmCompound != null ? cmmCompound.getPcID() : null,
              cmmCompound != null ? cmmCompound.getChebiID() : null,
              cmmCompound != null ? cmmCompound.getInHouseID() : null,
              cmmCompound != null ? cmmCompound.getAspergillusID() : null,
              cmmCompound != null ? cmmCompound.getKnapsackID() : null,
              cmmCompound != null ? cmmCompound.getNpatlasID() : null,
              cmmCompound != null ? cmmCompound.getFahfaID() : null,
              cmmCompound != null ? cmmCompound.getOhPositionID() : null,
              cmmCompound != null ? cmmCompound.getAspergillusWebName() : null);
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
