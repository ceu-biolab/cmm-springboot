package ceu.biolab.cmm.shared.domain.compound;

import ceu.biolab.FormulaType;
import ceu.biolab.cmm.rtSearch.model.compound.LipidMapsClassification;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@SuperBuilder
public class Compound {
    private final int compoundId;
    private final String casId;
    private final String compoundName;
    private final String formula;
    private final double mass;
    private final int chargeType;
    private final int chargeNumber;
    private final FormulaType formulaType;
    private final int compoundType;
    private final int compoundStatus;
    private final Integer formulaTypeInt;
    private final Double logP;
    private final Double rtPred;
    private final String inchi;
    private final String inchiKey;
    private final String smiles;
    private final String lipidType;
    private final Integer numChains;
    private final Integer numCarbons;
    private final Integer doubleBonds;
    private Set<LipidMapsClassification> lipidMapsClassifications;
    private final String biologicalActivity;
    private final String meshNomenclature;
    private final String iupacClassification;
    private final String mol2;

    public Compound(int compoundId, String casId, String compoundName, String formula, double mass,
                    int chargeType, int chargeNumber, FormulaType formulaType, int compoundType,
                    int compoundStatus, int formulaTypeInt, Double logP, Double rtPred, String inchi, String inchiKey, String smiles, String lipidType,
                    Integer numChains, Integer numCarbons, Integer doubleBonds, String biologicalActivity,
                    String meshNomenclature, String iupacClassification, String mol2) {
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
                + ", iupacClassification='" + iupacClassification + '\'' + ", mol2='" + mol2 + '\'' +
                '}';
    }
}


