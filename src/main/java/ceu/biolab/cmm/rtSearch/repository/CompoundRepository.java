package ceu.biolab.cmm.rtSearch.repository;

import ceu.biolab.*;
import ceu.biolab.cmm.rtSearch.dto.CompoundDTO;
import ceu.biolab.cmm.rtSearch.model.compound.CompoundMapper;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.adduct.AdductProcessing;
import ceu.biolab.cmm.shared.domain.adduct.AdductTransformer;
import ceu.biolab.cmm.shared.domain.compound.CMMCompound;
import ceu.biolab.cmm.rtSearch.model.compound.LipidMapsClassification;
import ceu.biolab.cmm.shared.domain.Database;

import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.msFeature.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.*;


//public List<TheoreticalCompounds> findRangeSimple (simple search)
@Repository
public class CompoundRepository {


    private static final Logger logger = LoggerFactory.getLogger(CompoundRepository.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<AnnotatedFeature> annotateMSFeature(Double mz, MzToleranceMode mzToleranceMode,
                                            Double tolerance, IonizationMode ionizationMode,
                                            Set<String> adductsString, Set<Database> databases,
                                            MetaboliteType metaboliteType)
            throws IncorrectAdduct, NotFoundElement, IncorrectFormula {

        List<AnnotatedFeature> annotatedMSFeature = new ArrayList<>();
        Integer compoundType = null;

        if (mz == null || tolerance == null || mzToleranceMode == null || ionizationMode == null) {
            return annotatedMSFeature;
        }

        if(metaboliteType == MetaboliteType.ALLEXCEPTPEPTIDES){
            compoundType = 0;
        }else if(metaboliteType == MetaboliteType.ONLYLIPIDS){
            compoundType = 1;
        }

        double lowerBound, upperBound;

        //solo puede ser positivo: verificar : crear clase PositiveDouble en el constructor final si es menor que 0 ERROR
        //mz en un rango: positive double (utilizando la misma clase)
        //dto cmmcompound

        try {
            IMSFeature msFeature = new MSFeature(mz, 0.0);
            AnnotatedFeature annotatedFeature = new AnnotatedFeature(msFeature);
            for (String adductString : adductsString) {
                Set<Compound> compoundsSet = new HashSet<>();

                Adduct adduct = AdductProcessing.getAdductFromString(adductString, ionizationMode, mz);
                double adductMass = adduct.getAdductMass();

                logger.info("adduct : {}", adductString);
                logger.info("adduct mass: {}", adductMass);

                AnnotationsByAdduct annotationsByAdduct = null;

                for (AnnotationsByAdduct annotation : annotatedFeature.getAnnotationsByAdducts()) {
                    if (annotation.getAdduct().equals(adductString)) {
                        annotationsByAdduct = annotation;
                        break;
                    }
                }

                if (annotationsByAdduct == null) {
                    annotationsByAdduct = new AnnotationsByAdduct(adductString, new ArrayList<>());
                    annotatedFeature.getAnnotationsByAdducts().add(annotationsByAdduct);
                }

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
                if (databases.contains(Database.CHEBI)) {
                    databaseConditions.add("c.chebi_id IS NOT NULL");
                }
                if (databases.contains(Database.PUBCHEM)) {
                    databaseConditions.add("c.pubchem_id IS NOT NULL");
                }
                if (databases.contains(Database.NPATLAS)) {
                    databaseConditions.add("c.npatlas_id IS NOT NULL");
                }

                // TODO CREATE QUERY CON MZS SEGUN ADUCTO LA MZ ES LA MZ +- ADUCTO (POSITIVO)
                double monoIsotopicMassFromMZAndAdduct = AdductTransformer.getMonoisotopicMassFromMZ(mz, adductString, ionizationMode);

                // Calculate tolerance range based on PPM or DA
                if (mzToleranceMode == MzToleranceMode.MDA) {
                    lowerBound = monoIsotopicMassFromMZAndAdduct - tolerance/1000;
                    upperBound = monoIsotopicMassFromMZAndAdduct + tolerance/1000;
                } else { // PPM (Parts Per Million)
                    double tolerancePPM = mz * tolerance / 1_000_000.0;

                    lowerBound = monoIsotopicMassFromMZAndAdduct - tolerancePPM;
                    upperBound = monoIsotopicMassFromMZAndAdduct + tolerancePPM;
                }

                final Integer compoundTypeFinal = compoundType;
                final double lowerBoundFinal = lowerBound;
                final double upperBoundFinal = upperBound;

                String sql = "SELECT c.* FROM compounds_view c WHERE ";
                sql += "c.mass BETWEEN " + lowerBoundFinal + " AND " + upperBoundFinal;
                if(metaboliteType == MetaboliteType.ALLEXCEPTPEPTIDES || metaboliteType == MetaboliteType.ONLYLIPIDS){
                    sql += " AND c.compound_type = " + compoundTypeFinal;
                }
                if (!databaseConditions.isEmpty()) {
                    sql += " AND (" + String.join(" OR ", databaseConditions) + ")";
                }

                String finalSql = sql;
                Set<Compound> compounds = jdbcTemplate.query(
                        finalSql, rs -> {
                            Set<CompoundDTO> compoundDTOS = new HashSet<>();
                            logger.info("Adduct: {}, bounds: {} - {}", adductString, lowerBoundFinal, upperBoundFinal);
                            logger.info("Query: {}", finalSql);

                            while (rs.next()) {
                                CompoundDTO dto = CompoundMapper.fromResultSet(rs);
                                compoundsSet.add(CompoundMapper.toDomain(dto));
                            }
                            return compoundsSet;
                        });

                List<Annotation> annotations = new ArrayList<>();
                for (Compound compound : compounds) {
                    annotations.add(new Annotation(compound));
                }
                annotationsByAdduct.setAnnotations(annotations);
            }
            annotatedMSFeature.add(annotatedFeature);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
        return annotatedMSFeature;
    }
}




/*
package ceu.biolab.cmm.rtSearch.repository;

import ceu.biolab.*;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.adduct.AdductProcessing;
import ceu.biolab.cmm.shared.domain.adduct.AdductTransformer;
import ceu.biolab.cmm.shared.domain.compound.CMMCompound;

import ceu.biolab.cmm.rtSearch.model.compound.LipidMapsClassification;

import ceu.biolab.cmm.shared.domain.Database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.*;


//public List<TheoreticalCompounds> findRangeSimple (simple search)
@Repository
public class CompoundRepository {


    private static final Logger logger = LoggerFactory.getLogger(CompoundRepository.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Set<MSFeature> annotateMSFeature(Double mz, MzToleranceMode mzToleranceMode,
                                            Double tolerance, IonizationMode ionizationMode,
                                            Set<String> adductsString, Set<Database> databases,
                                            MetaboliteType metaboliteType)
            throws IncorrectAdduct, NotFoundElement, IncorrectFormula {

        Set<MSFeature> annotatedMSFeature = new HashSet<>();
        Integer compoundType = null;

        if (mz == null || tolerance == null || mzToleranceMode == null || ionizationMode == null) {
            return annotatedMSFeature;
        }

        if(metaboliteType == MetaboliteType.ALLEXCEPTPEPTIDES){
            compoundType = 0;
        }else if(metaboliteType == MetaboliteType.ONLYLIPIDS){
            compoundType = 1;
        }

        double lowerBound, upperBound;

        //solo puede ser positivo: verificar : crear clase PositiveDouble en el constructor final si es menor que 0 ERROR
        //mz en un rango: positive double (utilizando la misma clase)
        //dto cmmcompound

        try {
            MSFeature msFeature = new MSFeature(mz);
            for (String adductString : adductsString) {
                //List<String> allAdductsIonization = AdductProcessing.getAllAdducts(ionizationMode);
                //String adductFromList = AdductProcessing.getadductString(adductString, allAdductsIonization);


                Set<GroupedCompoundsByAdduct> groupedCompoundsByAdduct = msFeature.getPotentialAnnotations();
                Set<CMMCompound> compoundsSet = new HashSet<>();

                Adduct adduct = AdductProcessing.getAdductFromString(adductString, ionizationMode, mz);
                double adductMass = adduct.getAdductMass();

                logger.info("adduct : {}", adductString);
                logger.info("adduct mass: {}", adductMass);

                GroupedCompoundsByAdduct groupedCompounds = new GroupedCompoundsByAdduct(adductString);
                msFeature.getPotentialAnnotations().add(groupedCompounds);

                for (GroupedCompoundsByAdduct groupedCompoundByAdduct : groupedCompoundsByAdduct) {
                    if (groupedCompoundByAdduct.getAdduct().equals(adductString)) {
                        groupedCompounds = groupedCompoundByAdduct;
                        break;
                    }
                }

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
                if (databases.contains(Database.CHEBI)) {
                    databaseConditions.add("c.chebi_id IS NOT NULL");
                }
                if (databases.contains(Database.PUBCHEM)) {
                    databaseConditions.add("c.pubchem_id IS NOT NULL");
                }
                if (databases.contains(Database.NPATLAS)) {
                    databaseConditions.add("c.npatlas_id IS NOT NULL");
                }


                // TODO CREATE QUERY CON MZS SEGUN ADUCTO LA MZ ES LA MZ +- ADUCTO (POSITIVO)
                double monoIsotopicMassFromMZAndAdduct = AdductTransformer.getMonoisotopicMassFromMZ(mz, adductString, ionizationMode);

                // Calculate tolerance range based on PPM or DA
                if (mzToleranceMode == MzToleranceMode.MDA) {
                    lowerBound = monoIsotopicMassFromMZAndAdduct - tolerance/1000;
                    upperBound = monoIsotopicMassFromMZAndAdduct + tolerance/1000;
                } else { // PPM (Parts Per Million)
                    double tolerancePPM = mz * tolerance / 1_000_000.0;

                    lowerBound = monoIsotopicMassFromMZAndAdduct - tolerancePPM;
                    upperBound = monoIsotopicMassFromMZAndAdduct + tolerancePPM;
                }

                final Integer compoundTypeFinal = compoundType;
                final double lowerBoundFinal = lowerBound;
                final double upperBoundFinal = upperBound;

                String sql = "SELECT c.* FROM compounds_view c WHERE ";
                sql += "c.mass BETWEEN " + lowerBoundFinal + " AND " + upperBoundFinal;
                if(metaboliteType == MetaboliteType.ALLEXCEPTPEPTIDES || metaboliteType == MetaboliteType.ONLYLIPIDS){
                    sql += " AND c.compound_type = " + compoundTypeFinal;
                }
                if (!databaseConditions.isEmpty()) {
                    sql += " AND (" + String.join(" OR ", databaseConditions) + ")";
                }

                String finalSql = sql;
                Set<CMMCompound> cmmCompounds = jdbcTemplate.query(
                        finalSql, rs -> {
                            logger.info("Adduct: {}, bounds: {} - {}", adductString, lowerBoundFinal, upperBoundFinal);
                            logger.info("Query: {}", finalSql);

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
                                FormulaType formulaType = null; //! this in compounds!
                                Integer compoundStatus = rs.getInt("compound_status");
                                Integer formulaTypeInt = rs.getInt("formula_type_int");
                                Double logP = rs.getDouble("logP");
                                Double rtPred = rs.getDouble("rt_pred");
                                String keggID = rs.getString("kegg_id");
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

                                CMMCompound cmmCompound = new CMMCompound(compoundID, casID, compoundName, formula, compoundMass, chargeTypeCompound, chargeNumber, formulaType,
                                        compoundTypeFinal, compoundStatus, formulaTypeInt, logP, rtPred, inchi, inchiKey, smiles, lipidType,
                                        numChains, numCarbons, doubleBonds, biologicalActivity, meshNomenclature, iupacClassification, keggID, lmID, hmdbID, agilentID, pcID, chebiID, inHouseID,
                                        aspergillusID, knapsackID, npatlasID, fahfaID, ohPositionID, aspergillusWebName);

                                cmmCompound.getLipidMapsClassifications().add(lmClassification);
                                compoundsSet.add(cmmCompound);
                            }
                            return compoundsSet;
                        });

                for (CMMCompound cmmCompound : cmmCompounds) {
                    groupedCompounds.getCmm_compounds().add(cmmCompound);
                }
                annotatedMSFeature.add(msFeature);
            }
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
        return annotatedMSFeature;
    }
}

 */
