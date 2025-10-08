package ceu.biolab.cmm.msSearch.dto;

import ceu.biolab.cmm.msSearch.domain.compound.LipidMapsClassification;
import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.shared.domain.compound.CompoundType;
import ceu.biolab.cmm.shared.domain.compound.Pathway;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@Data
public class CompoundDTO {
    private final int compoundId;
    private final String casId;
    private final String compoundName;
    private final String formula;
    private final double mass;
    private final int chargeType;
    private final int chargeNumber;
    private final FormulaType formulaType;
    private final CompoundType compoundType;
    private final Double logP;
    private final Double rtPred;
    private final String inchi;
    private final String inchiKey;
    private final String smiles;
    private final String lipidType;
    private final Integer numChains;
    private final Integer numberCarbons;
    private final Integer doubleBonds;
    private Set<LipidMapsClassification> lipidMapsClassifications;
    private final String biologicalActivity;
    private final String meshNomenclature;
    private final String iupacClassification;
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
    private final String aspergillusWebName;
    private final String mol2;
    private Set<Pathway> pathways;

    public CompoundDTO(int compoundId, String casId, String compoundName, String formula, double mass, int chargeType, int chargeNumber,
                       FormulaType formulaType, CompoundType compoundType, Double logP, Double rtPred, String inchi,
                       String inchiKey, String smiles, String lipidType, Integer numChains, Integer numberCarbons, Integer doubleBonds, String category,
                       String mainClass, String subClass, String classLevel4, String biologicalActivity, String meshNomenclature, String iupacClassification,
                       String keggID, String lmID, String hmdbID, String agilentID, Integer pcID, Integer chebiID, String inHouseID, Integer aspergillusID,
                       String knapsackID, Integer npatlasID, Integer fahfaID, Integer ohPositionID, String aspergillusWebName, String mol2, Set<Pathway> pathways) {
        this.compoundId = compoundId;
        this.casId = casId;
        this.compoundName = compoundName;
        this.formula = formula;
        this.mass = mass;
        this.chargeType = chargeType;
        this.chargeNumber = chargeNumber;
        FormulaType inferredFormulaType = FormulaType.inferFromFormula(formula).orElse(null);
        this.formulaType = formulaType != null ? formulaType : inferredFormulaType;
        this.compoundType = compoundType;
        this.logP = logP;
        this.rtPred = rtPred;
        this.inchi = inchi;
        this.inchiKey = inchiKey;
        this.smiles = smiles;
        this.lipidType = lipidType;
        this.numChains = numChains;
        this.numberCarbons = numberCarbons;
        this.doubleBonds = doubleBonds;
        this.biologicalActivity = biologicalActivity;
        this.meshNomenclature = meshNomenclature;
        this.iupacClassification = iupacClassification;
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
        this.aspergillusWebName = aspergillusWebName;
        this.lipidMapsClassifications = new HashSet<>();
        LipidMapsClassification lipidMapsClassification = new LipidMapsClassification(category, mainClass, subClass, classLevel4);
        this.getLipidMapsClassifications().add(lipidMapsClassification);
        this.mol2 = mol2;
        this.pathways = pathways;
    }

    public CompoundDTO(ResultSet rs) throws SQLException {
        this.compoundId = rs.getInt("compound_id");
        this.casId = rs.getString("cas_id");
        this.compoundName = rs.getString("compound_name");
        this.formula = rs.getString("formula");
        this.mass = rs.getDouble("mass");
        this.chargeType = rs.getInt("charge_type");
        this.chargeNumber = rs.getInt("charge_number");
        Integer compoundTypeValue = null;
        Object compoundTypeRaw = rs.getObject("compound_type");
        if (compoundTypeRaw instanceof Number number) {
            compoundTypeValue = number.intValue();
        }
        CompoundType parsedCompoundType = CompoundType.fromDbValue(compoundTypeValue);
        this.compoundType = parsedCompoundType != null ? parsedCompoundType : CompoundType.NON_LIPID;
        this.formulaType = FormulaType.inferFromFormula(this.formula).orElse(null);
        this.logP = rs.getDouble("logP");
        this.rtPred = rs.getDouble("rt_pred");
        this.inchi = rs.getString("inchi");
        this.inchiKey = rs.getString("inchi_key");
        this.smiles = rs.getString("smiles");
        this.lipidType = rs.getString("lipid_type");
        this.numChains = rs.getInt("num_chains");
        this.numberCarbons = rs.getInt("number_carbons");
        this.doubleBonds = rs.getInt("double_bonds");
        this.biologicalActivity = rs.getString("biological_activity");
        this.meshNomenclature = rs.getString("mesh_nomenclature");
        this.iupacClassification = rs.getString("iupac_classification");
        this.keggID = rs.getString("kegg_id");
        this.hmdbID = rs.getString("hmdb_id");
        this.lmID = rs.getString("lm_id");
        this.agilentID = rs.getString("agilent_id");
        this.pcID = rs.getInt("pc_id");
        this.chebiID = rs.getInt("chebi_id");
        this.inHouseID = rs.getString("in_house_id");
        this.aspergillusID = rs.getInt("aspergillus_id");
        this.knapsackID = rs.getString("knapsack_id");
        this.npatlasID = rs.getInt("npatlas_id");
        this.fahfaID = rs.getInt("fahfa_id");
        this.ohPositionID = rs.getInt("oh_position");
        this.aspergillusWebName = rs.getString("aspergillus_web_name");
        String category = rs.getString("category");
        String mainClass = rs.getString("main_class");
        String subClass = rs.getString("sub_class");
        String classLevel4 = rs.getString("class_level4");
        LipidMapsClassification lipidMapsClassification = new LipidMapsClassification(category, mainClass, subClass, classLevel4);
        this.getLipidMapsClassifications().add(lipidMapsClassification);
        this.mol2 = rs.getString("mol2");
        this.pathways = new HashSet<>();
    }

    public Set<LipidMapsClassification> getLipidMapsClassifications() {
        return lipidMapsClassifications;
    }

    public void setLipidMapsClassifications(Set<LipidMapsClassification> lipidMapsClassifications) {
        this.lipidMapsClassifications = lipidMapsClassifications;
    }
}
