package ceu.biolab.cmm.shared.domain.compound;

import ceu.biolab.FormulaType;
import ceu.biolab.cmm.rtSearch.model.compound.LipidMapsClassification;

import java.util.HashSet;
import java.util.Set;

public class CMMCompound extends Compound implements Comparable<CMMCompound>{

    private final String keggID;
    private final String lmID;
    private final String hmdbID;
    private final String agilentID;
    private final Integer pcID;
    private final Integer chebiID;
    private final String inHouseID;
    private final Integer aspergillusID;
    private final String knapsackID;
    private final Integer npatlasID;
    private final Integer fahfaID;
    private final Integer ohPositionID;
    private final String biologicalActivity;
    private final String meshNomenclature;
    private final String iupacClassification;
    private final String aspergillusWebName;
    //! cambiar a compound


    public CMMCompound(int compoundId, String casId, String compoundName, String formula, double mass,
                       int chargeType, int chargeNumber, FormulaType formulaType, int compoundType,
                       int compoundStatus, int formulaTypeInt, Double logP, Double rtPred, String inchi, String inchiKey, String smiles, String lipidType,
                       Integer numChains, Integer numCarbons, Integer doubleBonds, String keggID, String lmID, String hmdbID, String agilentID, Integer pcID, Integer chebiID, String inHouseID,
                       Integer aspergillusID, String knapsackID, Integer npatlasID, Integer fahfaID, Integer ohPositionID, String biologicalActivity,
                       String meshNomenclature, String iupacClassification, String aspergillusWebName) {

        super(compoundId, casId, compoundName, formula, mass, chargeType, chargeNumber, formulaType, compoundType, compoundStatus, formulaTypeInt, logP, rtPred, inchi, inchiKey, smiles, lipidType, numChains, numCarbons, doubleBonds);
        this.keggID = keggID;
        this.lmID = lmID;
        this.hmdbID = hmdbID;
        this.agilentID = agilentID;
        this.pcID = pcID;
        this.chebiID = chebiID;
        this.inHouseID = inHouseID;
        this.aspergillusID = aspergillusID;
        this.knapsackID = knapsackID;
        this.npatlasID = npatlasID;
        this.fahfaID = fahfaID;
        this.ohPositionID = ohPositionID;
        this.biologicalActivity = biologicalActivity;
        this.meshNomenclature = meshNomenclature;
        this.iupacClassification = iupacClassification;
        this.aspergillusWebName = aspergillusWebName;
    }

    public String getkeggID() {
        return keggID;
    }

    public String getLmID() {
        return lmID;
    }

    public String getHmdbID() {
        return hmdbID;
    }

    public String getAgilentID() {
        return agilentID;
    }

    public Integer getPcID() {
        return pcID;
    }

    public Integer getChebiID() {
        return chebiID;
    }

    public String getInHouseID() {
        return inHouseID;
    }

    public Integer getAspergillusID() {
        return aspergillusID;
    }

    public String getKnapsackID() {
        return knapsackID;
    }

    public Integer getNpatlasID() {
        return npatlasID;
    }

    public Integer getFahfaID() {
        return fahfaID;
    }

    public Integer getOhPositionID() {
        return ohPositionID;
    }

    public String getBiologicalActivity() {
        return biologicalActivity;
    }

    public String getMeshNomenclature() {
        return meshNomenclature;
    }

    public String getIupacClassification() {
        return iupacClassification;
    }

    public String getAspergillusWebName() {
        return aspergillusWebName;
    }


    @Override
    public int compareTo(CMMCompound other) {
        return Integer.compare(this.getCompoundId(), other.getCompoundId());
    }

    public int getCompoundId() {
        return super.getCompoundId();
    }

    @Override
    public String toString() {
        return "CMMCompound{" +
                super.toString() +
                "keggID='" + keggID + '\'' + ", lmID='" + lmID + '\'' + ", hmdbID='" + hmdbID + '\'' +
                ", agilentID='" + agilentID + '\'' + ", pcID=" + pcID + ", chebiID=" + chebiID +
                ", inHouseID='" + inHouseID + '\'' + ", aspergillusID=" + aspergillusID + ", knapsackID='" + knapsackID + '\'' +
                ", npatlasID=" + npatlasID + ", fahfaID=" + fahfaID + ", ohPositionID=" + ohPositionID +
                ", biologicalActivity='" + biologicalActivity + '\'' + ", meshNomenclature='" + meshNomenclature + '\''
                + ", iupacClassification='" + iupacClassification + '\'' + ", aspergillusWebName='" + aspergillusWebName + '\'' +
                '}';
    }
}
