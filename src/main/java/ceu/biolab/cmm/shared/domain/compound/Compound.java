package ceu.biolab.cmm.shared.domain.compound;

import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.msSearch.domain.compound.LipidMapsClassification;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@SuperBuilder
public class Compound {
    private int compoundId;
    private String casId;
    private String compoundName;
    private String formula;
    private double mass;
    private int chargeType;
    private int chargeNumber;
    private FormulaType formulaType;
    // TODO this should not be an int?
    private int compoundType;
    private int compoundStatus;
    private Integer formulaTypeInt;
    private Double logP;
    private Double rtPred;
    private String inchi;
    private String inchiKey;
    private String smiles;
    private String lipidType;
    private Integer numChains;
    private Integer numCarbons;
    private Integer doubleBonds;
    private Set<LipidMapsClassification> lipidMapsClassifications;
    private String biologicalActivity;
    private String meshNomenclature;
    private String iupacClassification;
    private String mol2;
    private Set<Pathway> pathways;

// TODO add pathways

    public Compound() {
        this.lipidMapsClassifications = new HashSet<>();
    }

    public Compound(Compound compound) {
        this.compoundId = compound.compoundId;
        this.casId = compound.casId;
        this.compoundName = compound.compoundName;
        this.formula = compound.formula;
        this.mass = compound.mass;
        this.chargeType = compound.chargeType;
        this.chargeNumber = compound.chargeNumber;
        this.formulaType = compound.formulaType;
        this.compoundType = compound.compoundType;
        this.compoundStatus = compound.compoundStatus;
        this.formulaTypeInt = compound.formulaTypeInt;
        this.logP = compound.logP;
        this.rtPred = compound.rtPred;
        this.inchi = compound.inchi;
        this.inchiKey = compound.inchiKey;
        this.smiles = compound.smiles;
        this.lipidType = compound.lipidType;
        this.numChains = compound.numChains;
        this.numCarbons = compound.numCarbons;
        this.doubleBonds = compound.doubleBonds;
        this.lipidMapsClassifications = new HashSet<>(compound.lipidMapsClassifications);
    }

    public Compound(int compoundId, String casId, String compoundName, String formula, double mass,
                    int chargeType, int chargeNumber, FormulaType formulaType, int compoundType,
                    int compoundStatus, int formulaTypeInt, Double logP, Double rtPred, String inchi, String inchiKey, String smiles, String lipidType,
                    Integer numChains, Integer numCarbons, Integer doubleBonds, String biologicalActivity,
                    String meshNomenclature, String iupacClassification, String mol2, Set<Pathway> pathways) {
        this.compoundId = compoundId;
        this.casId = casId;
        this.compoundName = compoundName;
        this.formula = formula;
        this.mass = mass;
        this.chargeType = chargeType;
        this.chargeNumber = chargeNumber;
        this.formulaType = formulaType;
        this.compoundType = compoundType;
        this.compoundStatus = compoundStatus;
        this.formulaTypeInt = formulaTypeInt;
        this.logP = logP;
        this.rtPred = rtPred;
        this.inchi = inchi;
        this.inchiKey = inchiKey;
        this.smiles = smiles;
        this.lipidType = lipidType;
        this.numChains = numChains;
        this.numCarbons = numCarbons;
        this.doubleBonds = doubleBonds;
        this.lipidMapsClassifications = new HashSet<>();
        this.biologicalActivity = biologicalActivity;
        this.meshNomenclature = meshNomenclature;
        this.iupacClassification = iupacClassification;
        this.mol2 = mol2;
        this.pathways = pathways;
    }

    public int getCompoundId() {
        return compoundId;
    }

    public String getCasId() {
        return casId;
    }

    public String getCompoundName() {
        return compoundName;
    }

    public String getFormula() {
        return formula;
    }

    public double getMass() {
        return mass;
    }

    public int getChargeType() {
        return chargeType;
    }

    public int getChargeNumber() {
        return chargeNumber;
    }

    public FormulaType getFormulaType() {
        return formulaType;
    }

    public int getCompoundType() {
        return compoundType;
    }

    public int getCompoundStatus() {
        return compoundStatus;
    }

    public Integer getFormulaTypeInt() {
        return formulaTypeInt;
    }

    public Double getLogP() {
        return logP;
    }

    public Double getRtPred() {
        return rtPred;
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

    public String getBiologicalActivity() {
        return biologicalActivity;
    }

    public String getMeshNomenclature() {
        return meshNomenclature;
    }

    public String getIupacClassification() {
        return iupacClassification;
    }

    public String getMol2() {
        return mol2;
    }

    public Set<Pathway> getPathways() {
        return pathways;
    }

    public void setPathways(Set<Pathway> pathways) {
        this.pathways = pathways;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.compoundId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Compound compound = (Compound) obj;
        return Objects.equals(this.compoundId, compound.compoundId);
    }


    @Override
    public String toString() {
        return "Compound{" +
                "compoundId=" + this.compoundId +
                ", casId='" + this.casId + '\'' +
                ", compoundName='" + this.compoundName + '\'' +
                ", formula='" + this.formula + '\'' +
                ", mass=" + this.mass +
                ", chargeType=" + this.chargeType +
                ", chargeNumber=" + this.chargeNumber +
                ", formulaType=" + this.formulaType +
                ", compoundType=" + this.compoundType +
                ", compoundStatus=" + this.compoundStatus +
                ", formulaTypeInt=" + this.formulaTypeInt +
                ", logP=" + this.logP +
                ", rtPred=" + this.rtPred +
                ", inchi='" + inchi + '\'' + ", inchiKey='" + inchiKey + '\'' + ", smiles='" + smiles + '\'' +
                ", lipidType='" + lipidType + '\'' + ", numChains=" + numChains + ", numCarbons=" + numCarbons +
                ", doubleBonds=" + doubleBonds + ", classification='" + lipidMapsClassifications +
                ", biologicalActivity='" + biologicalActivity + '\'' + ", meshNomenclature='" + meshNomenclature + '\''
                + ", iupacClassification='" + iupacClassification + '\'' + ", mol2='" + mol2 + '\''
                + ", pathways='" + pathways + '}';
    }
}


