package ceu.biolab.cmm.rtSearch.model.compound;

import ceu.biolab.cmm.rtSearch.dto.CompoundDTO;
import ceu.biolab.cmm.shared.domain.compound.CMMCompound;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.compound.Pathway;
import ceu.biolab.cmm.shared.domain.FormulaType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class CompoundMapper {

    public static CompoundDTO fromResultSet(ResultSet rs) throws SQLException {
        FormulaType formulaType = null;
        Set<Pathway> pathways = new HashSet<>();
        return new CompoundDTO(
                rs.getInt("compound_id"),
                rs.getString("cas_id"),
                rs.getString("compound_name"),
                rs.getString("formula"),
                rs.getDouble("mass"),
                rs.getInt("charge_type"),
                rs.getInt("charge_number"),
                formulaType = FormulaType.getFormulTypefromInt(rs.getInt("formula_type_int")),
                rs.getInt("compound_type"),
                rs.getInt("compound_status"),
                rs.getInt("formula_type_int"),
                rs.getDouble("logP"),
                rs.getDouble("rt_pred"),
                rs.getString("inchi"),
                rs.getString("inchi_key"),
                rs.getString("smiles"),
                rs.getString("lipid_type"),
                rs.getInt("num_chains"),
                rs.getInt("number_carbons"),
                rs.getInt("double_bonds"),
                rs.getString("category"),
                rs.getString("main_class"),
                rs.getString("sub_class"),
                rs.getString("class_level4"),
                rs.getString("biological_activity"),
                rs.getString("mesh_nomenclature"),
                rs.getString("iupac_classification"),
                rs.getString("kegg_id"),
                rs.getString("lm_id"),
                rs.getString("hmdb_id"),
                rs.getString("agilent_id"),
                rs.getInt("pc_id"),
                rs.getInt("chebi_id"),
                rs.getString("in_house_id"),
                rs.getInt("aspergillus_id"),
                rs.getString("knapsack_id"),
                rs.getInt("npatlas_id"),
                rs.getInt("fahfa_id"),
                rs.getInt("oh_position"),
                rs.getString("aspergillus_web_name"),
                rs.getString("mol2"),
                pathways
        );
    }

    public static Compound toCompound(CompoundDTO compoundDTO) {
        Set<LipidMapsClassification> lipidMapsClassifications = new HashSet<>();
        Set<LipidMapsClassification> lipidMapsClassificationsSet = compoundDTO.getLipidMapsClassifications();
        for(LipidMapsClassification lipidMapsClassification : lipidMapsClassificationsSet) {
            if (lipidMapsClassification.getCategory() != null || lipidMapsClassification.getMainClass() != null || lipidMapsClassification.getSubClass() != null || lipidMapsClassification.getClassLevel4() != null) {
                lipidMapsClassifications.add(new LipidMapsClassification(
                        lipidMapsClassification.getCategory(),
                        lipidMapsClassification.getMainClass(),
                        lipidMapsClassification.getSubClass(),
                        lipidMapsClassification.getClassLevel4()
                ));
            }
        }

        Compound compound = new CMMCompound(
                compoundDTO.getCompoundId(), compoundDTO.getCasId(), compoundDTO.getCompoundName(), compoundDTO.getFormula(),
                compoundDTO.getMass(), compoundDTO.getChargeType(), compoundDTO.getChargeNumber(), compoundDTO.getFormulaType(),
                compoundDTO.getCompoundType(), compoundDTO.getCompoundStatus(), compoundDTO.getFormulaTypeInt(),
                compoundDTO.getLogP(), compoundDTO.getRtPred(), compoundDTO.getInchi(), compoundDTO.getInchiKey(), compoundDTO.getSmiles(),
                compoundDTO.getLipidType(), compoundDTO.getNumChains(), compoundDTO.getNumberCarbons(), compoundDTO.getDoubleBonds(),
                compoundDTO.getBiologicalActivity(), compoundDTO.getMeshNomenclature(), compoundDTO.getIupacClassification(),
                compoundDTO.getMol2(), compoundDTO.getPathways(),
                compoundDTO.getKeggID(), compoundDTO.getLmID(), compoundDTO.getHmdbID(), compoundDTO.getAgilentID(),
                compoundDTO.getPcID(), compoundDTO.getChebiID(), compoundDTO.getInHouseID(), compoundDTO.getAspergillusID(),
                compoundDTO.getKnapsackID(), compoundDTO.getNpatlasID(), compoundDTO.getFahfaID(), compoundDTO.getOhPositionID(),
                compoundDTO.getAspergillusWebName()
        );

        compound.setLipidMapsClassifications(lipidMapsClassifications);
        return compound;
    }
}
