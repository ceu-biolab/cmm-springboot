package ceu.biolab.cmm.rtSearch.model.compound;

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
    private final String inchi;
    private final String inchiKey;
    private final String smiles;
    private final String lipidType;
    private final Integer numChains;
    private final Integer numCarbons;
    private final Integer doubleBonds;
    private Set<LipidMapsClassification> lipidMapsClassifications;



    public CMMCompound(Integer compound_id, String cas_id, String compound_name, String formula, Double mass, Integer charge_type,
                       Integer charge_number, String formula_type, Integer compound_type, Integer compound_status, Integer formula_type_int,
                       Double logP, Double rt_pred, String keggID, String lmID, String hmdbID, String agilentID, Integer pcID, Integer chebiID, String inHouseID,
                       Integer aspergillusID, String knapsackID, Integer npatlasID, Integer fahfaID, Integer ohPositionID, String biologicalActivity,
                       String meshNomenclature, String iupacClassification, String aspergillusWebName, String inchi, String inchiKey, String smiles, String lipidType,
                       Integer numChains, Integer numCarbons, Integer doubleBonds) {

        super(compound_id, cas_id, compound_name, formula, mass, charge_type, charge_number, formula_type, compound_type, compound_status, formula_type_int, logP, rt_pred);
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
        this.inchi = inchi;
        this.inchiKey = inchiKey;
        this.smiles = smiles;
        this.lipidType = lipidType;
        this.numChains = numChains;
        this.numCarbons = numCarbons;
        this.doubleBonds = doubleBonds;
        this.lipidMapsClassifications = new HashSet<>();
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

    public String getInchi() {
        return inchi;
    }

    public String getInchiKey() {
        return inchiKey;
    }

    public String getSmiles() {
        return smiles;
    }

    public String getLipidType() {
        return lipidType;
    }

    public Integer getNumChains() {
        return numChains;
    }

    public Integer getNumCarbons() {
        return numCarbons;
    }

    public Integer getDoubleBonds() {
        return doubleBonds;
    }

    public Set<LipidMapsClassification> getLipidMapsClassifications() {
        return lipidMapsClassifications;
    }

    public void setLipidMapsClassifications(Set<LipidMapsClassification> lipidMapsClassifications) {
        this.lipidMapsClassifications = lipidMapsClassifications;
    }


    @Override
    public int compareTo(CMMCompound other) {
        return Integer.compare(this.getCompoundId(), other.getCompoundId());
    }

    public Integer getCompoundId() {
        return super.getCompound_id();
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
                ", inchi='" + inchi + '\'' + ", inchiKey='" + inchiKey + '\'' + ", smiles='" + smiles + '\'' +
                ", lipidType='" + lipidType + '\'' + ", numChains=" + numChains + ", numCarbons=" + numCarbons +
                ", doubleBonds=" + doubleBonds + ", classification='" + lipidMapsClassifications + '}';
    }
}
