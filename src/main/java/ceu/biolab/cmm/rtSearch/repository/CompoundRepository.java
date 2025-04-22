package ceu.biolab.cmm.rtSearch.repository;

import ceu.biolab.*;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.adduct.AdductProcessing;
import ceu.biolab.cmm.shared.domain.adduct.AdductTransformer;
import ceu.biolab.cmm.shared.domain.compound.CMMCompound;
import ceu.biolab.cmm.rtSearch.model.compound.GroupedCompoundsByAdduct;
import ceu.biolab.cmm.rtSearch.model.compound.LipidMapsClassification;
import ceu.biolab.cmm.rtSearch.model.msFeature.MSFeature;
import ceu.biolab.cmm.shared.domain.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;


//public List<TheoreticalCompounds> findRangeSimple (simple search)
@Repository
public class CompoundRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Set<MSFeature> annotateMSFeature(Double mz, MzToleranceMode mzToleranceMode,
                                            Double tolerance, IonizationMode ionizationMode,
                                            Set<String> adductsString, Set<Database> databases,
                                            MetaboliteType metaboliteType)
            throws IncorrectAdduct, NotFoundElement, IncorrectFormula {

        Set<MSFeature> annotatedMSFeature = new HashSet<>();
        Integer compound_type = null;

        if (mz == null || tolerance == null || mzToleranceMode == null || ionizationMode == null) {
            return annotatedMSFeature;
        }

        if(metaboliteType == MetaboliteType.ALLEXCEPTPEPTIDES){
            compound_type = 0;
        }else if(metaboliteType == MetaboliteType.ONLYLIPIDS){
            compound_type = 1;
        }

        double lowerBound, upperBound;


        //solo puede ser positivo: verificar : crear clase PositiveDouble en el constructor final si es menor que 0 ERROR
        //mz en un rango: positive double (utilizando la misma clase)
        //dto cmmcompound


        try {
            for (String adductString : adductsString) {
                MSFeature msFeature = new MSFeature(mz, 0.0);
                // TODO
                // PRIMERO COGER ADUCTOS DE LA LISTA DE ADUCTOS PREDEFINIDOS.
                // ESTOS ADUCTOS PUEDEN SER CARGA 1, 2 o 3 y multimer 1, 2 o 3

                //List<String> allAdductsIonization = AdductProcessing.getAllAdducts(ionizationMode);
                //String adductFromList = AdductProcessing.getadductString(adductString, allAdductsIonization);

                Adduct adduct = AdductProcessing.getAdductFromString(adductString, ionizationMode, mz);
                double adductMass = adduct.getAdductMass();

                Set<GroupedCompoundsByAdduct> groupedCompoundsByAdduct = msFeature.getPotentialAnnotations();

                GroupedCompoundsByAdduct groupedCompounds = new GroupedCompoundsByAdduct(adductString);
                msFeature.getPotentialAnnotations().add(groupedCompounds);

                for (GroupedCompoundsByAdduct groupedCompoundByAdduct : groupedCompoundsByAdduct) {
                    if (groupedCompoundByAdduct.getAdduct().equals(adductString)) {
                        groupedCompounds = groupedCompoundByAdduct;
                        break;
                    }
                }

                // TODO CREATE QUERY CON MZS SEGUN ADUCTO LA MZ ES LA MZ +- ADUCTO (POSITIVO)
                double monoIsotopicMassFromMZAndAdduct = AdductTransformer.getMonoisotopicMassFromMZ(mz, adductString, ionizationMode);

                String sql = "SELECT c.* FROM compounds_view c " +
                "WHERE c.compound_type = ? AND c.mass BETWEEN ? AND ? ";

                List<String> databaseConditions = new ArrayList<>();

                if (databases.contains(Database.HMDB)) {
                    databaseConditions.add("c.hmdb_id IS NOT NULL");
                }
                if (databases.contains(Database.LIPIDMAPS)) {
                    databaseConditions.add("c.lm_id IS NOT NULL");
                }
                if (databases.contains(Database.KEGG)) {
                    databaseConditions.add("c.kegg_id IS NOT NULL");
                }
                if (databases.contains(Database.INHOUSE)) {
                    databaseConditions.add("c.in_house_id IS NOT NULL");
                }
                if (databases.contains(Database.ASPERGILLUS)) {
                    databaseConditions.add("c.aspergillus_id IS NOT NULL");
                }
                if (databases.contains(Database.FAHFA)) {
                    databaseConditions.add("c.fahfa_id IS NOT NULL");
                }
                /*if (databases.contains(Database.CHEBI)) {
                    databaseConditions.add("c.chebi_id IS NOT NULL");
                }
                if (databases.contains(Database.PUBCHEM)) {
                    databaseConditions.add("c.pubchem_id IS NOT NULL");
                }
                if (databases.contains(Database.NPATLAS)) {
                    databaseConditions.add("c.npatlas_id IS NOT NULL");
                }
                 */

                if (!databaseConditions.isEmpty()) {
                    sql += " AND " + String.join(" OR ", databaseConditions) + "";
                }


                // Calculate tolerance range based on PPM or DA
                double monoMassWithoutAdduct = monoIsotopicMassFromMZAndAdduct - adductMass;

                if (mzToleranceMode == MzToleranceMode.MDA) {
                    lowerBound = monoMassWithoutAdduct - tolerance;
                    upperBound = monoMassWithoutAdduct + tolerance;
                } else { // PPM (Parts Per Million)
                    double tolerancePPM = (monoMassWithoutAdduct * tolerance) / 1_000_000.0;
                    lowerBound = monoMassWithoutAdduct - tolerancePPM;
                    upperBound = monoMassWithoutAdduct + tolerancePPM;
                }

                final Integer compound_typeFinal = compound_type;
                final double lowerBoundFinal = lowerBound;
                final double upperBoundFinal = upperBound;

                String finalSql = sql;
                Set<CMMCompound> cmmCompounds = jdbcTemplate.query(
                        sql,
                        ps -> {
                    ps.setInt(1, compound_typeFinal);
                    ps.setDouble(2, lowerBoundFinal);
                    ps.setDouble(3, upperBoundFinal);
                    }, rs -> {
                            Set<CMMCompound> compoundsSet = new HashSet<>();


                            while (rs.next()) {
                                Integer compoundID = rs.getInt("compound_id");
                                String casID = rs.getString("cas_id");
                                String compoundName = rs.getString("compound_name");
                                String formula = rs.getString("formula");
                                Double compoundMass = rs.getDouble("mass");
                                Integer chargeTypeCompound = rs.getInt("charge_type");
                                Integer chargeNumber = rs.getInt("charge_number");
                                //String formulaTypeString = rs.getString("formula_type");
                                //FormulaType formulaType = FormulaType.valueOf(formulaTypeString);
                                FormulaType formulaType = null;
                                Integer compoundStatus = rs.getInt("compound_status");
                                Integer formulaTypeInt = rs.getInt("formula_type_int");
                                Double logP = rs.getDouble("logP");
                                Double rtPred = rs.getDouble("rt_pred");
                                String keggId = rs.getString("kegg_id");
                                String lmID = rs.getString("lm_id");
                                String hmdbID = rs.getString("hmdb_id");
                                String agilentID = rs.getString("agilent_id");
                                Integer pcID = rs.getInt("pc_id");
                                Integer chebiID = rs.getInt("chebi_id");
                                String inHouseID = rs.getString("in_house_id");
                                Integer aspergillusID = rs.getInt("aspergillus_id");
                                String knapsackID = rs.getString("knapsack_id");
                                Integer npatlasID = rs.getInt("npatlas_id");
                                Integer fahfaID = rs.getInt("fahfa_id");
                                Integer ohPositionID = rs.getInt("oh_position");
                                String biologicalActivity = rs.getString("biological_activity");
                                String meshNomenclature = rs.getString("mesh_nomenclature");
                                String iupacClassification = rs.getString("iupac_classification");
                                String aspergillusWebName = rs.getString("aspergillus_web_name");
                                String inchi = rs.getString("inchi");
                                String inchiKey = rs.getString("inchi_key");
                                String smiles = rs.getString("smiles");
                                String lipidType = rs.getString("lipid_type");
                                Integer numChains = rs.getInt("num_chains");
                                Integer numCarbons = rs.getInt("number_carbons");
                                Integer doubleBonds = rs.getInt("double_bonds");
                                String category = rs.getString("category");
                                String mainClass = rs.getString("main_class");
                                String subClass = rs.getString("sub_class");
                                String classLevel4 = rs.getString("class_level4");

                                // construccion de objetos de tipo no primitivo TODO
                                Set<LipidMapsClassification> lipidMapsClassifications = new HashSet<>();
                                LipidMapsClassification lmClassification = new LipidMapsClassification(category, mainClass, subClass, classLevel4);

                                // CMMCompound cmmCompound = new CMMCompound(compoundID, casID, compoundName, formula, compoundMass, chargeTypeCompound,
                                //         chargeNumber, formulaType, compound_typeFinal, compoundStatus, formulaTypeInt, logP, rtPred, inchi, inchiKey, smiles,
                                //         lipidType, numChains, numCarbons, doubleBonds,
                                //         keggId, lmID, hmdbID, agilentID, pcID, chebiID, inHouseID, aspergillusID, knapsackID, npatlasID, fahfaID,
                                //         ohPositionID, biologicalActivity, meshNomenclature, iupacClassification, aspergillusWebName);
                                // cmmCompound.getLipidMapsClassifications().add(lmClassification);

                                //compoundsSet.add(cmmCompound);
                            }
                            return compoundsSet;
                        });

                for (CMMCompound cmmCompound : cmmCompounds) {
                    groupedCompounds.getCmm_compounds().add(cmmCompound);
                    annotatedMSFeature.add(msFeature);
                }
            }
            //* modify to exceptions Adducts
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
        return annotatedMSFeature;
    }
}