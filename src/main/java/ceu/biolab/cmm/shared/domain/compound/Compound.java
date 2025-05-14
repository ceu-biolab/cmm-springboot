package ceu.biolab.cmm.shared.domain.compound;

import ceu.biolab.FormulaType;
import ceu.biolab.cmm.rtSearch.domain.compound.LipidMapsClassification;
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

    public void addPathway(Pathway pathway) {
        if (this.pathways == null) {
            this.pathways = new HashSet<>();
        }
        if (pathway != null && pathway.getPathwayId() != -1) {
            this.pathways.add(pathway);
        }
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
}
