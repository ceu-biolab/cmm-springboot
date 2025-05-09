package ceu.biolab.cmm.msSearch.repository;

import ceu.biolab.*;
import ceu.biolab.cmm.msSearch.dto.CompoundDTO;
import ceu.biolab.cmm.msSearch.model.compound.CompoundMapper;

import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.shared.service.adduct.AdductProcessing;
import ceu.biolab.cmm.shared.service.adduct.AdductTransformer;
import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.compound.Pathway;
import ceu.biolab.cmm.shared.domain.msFeature.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Optional;


@Repository
public class CompoundRepository {

    private static final Logger logger = LoggerFactory.getLogger(CompoundRepository.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * This method annotates the MS features
     * @param mz the experimental mass to search
     * @param mzToleranceMode the tolerance mode (mDa or ppm)
     * @param tolerance the tolerance as a Double
     * @param ionizationMode the ionization mode (positve or negative)
     * @param detectedAdduct the detected adduct as an optional for when LC-MS search
     * @param formulaType the formula type of the compound as an optional for when LC-MS Search
     * @param adductsString the set of adducts to search as a set of strings
     * @param databases the set of databases to match the compounds with
     * @param metaboliteType the metabolite type (peptides, lipids,...)
     * @return a list of annotated features
     */
    public List<AnnotatedFeature> annotateMSFeature(Double mz, MzToleranceMode mzToleranceMode,
                                            Double tolerance, IonizationMode ionizationMode, Optional<String> detectedAdduct, Optional<FormulaType> formulaType,
                                            Set<String> adductsString, Set<Database> databases,
                                            MetaboliteType metaboliteType) {
        List<AnnotatedFeature> annotatedMSFeature = new ArrayList<>();
        Integer compoundType = null;

        if (mz == null || tolerance == null || mzToleranceMode == null || ionizationMode == null) {
            return annotatedMSFeature;
        }

        if (metaboliteType == MetaboliteType.ONLYLIPIDS) {
            compoundType = 1;
        }

        double lowerBound, upperBound;

        //TODO solo puede ser positivo: verificar : crear clase PositiveDouble en el constructor final si es menor que 0 ERROR

        try {
            IMSFeature msFeature = new MSFeature(mz, 0.0);
            AnnotatedFeature annotatedFeature = new AnnotatedFeature(msFeature);
            Set<String> adductsToProcess;

            if (detectedAdduct != null && detectedAdduct.isPresent()) {
                adductsToProcess = Set.of(detectedAdduct.get());
            } else {
                adductsToProcess = adductsString;
            }

            if (adductsToProcess == null || adductsToProcess.isEmpty()) {
                return annotatedMSFeature;
            }

            for (String adductString : adductsToProcess) {

                Set<Compound> compoundsSet = new HashSet<>();
                Adduct adduct = AdductProcessing.getAdductFromString(adductString, ionizationMode, mz);
                double adductMass = adduct.getAdductMass();

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
                    databaseConditions.add("c.pc_id IS NOT NULL");
                }
                if (databases.contains(Database.NPATLAS)) {
                    databaseConditions.add("c.npatlas_id IS NOT NULL");
                }

                double monoIsotopicMassFromMZAndAdduct = AdductTransformer.getMonoisotopicMassFromMZ(mz, adductString, ionizationMode);

                // Calculate tolerance range based on PPM or DA
                if (mzToleranceMode == MzToleranceMode.MDA) {
                    lowerBound = monoIsotopicMassFromMZAndAdduct - tolerance/1000;
                    upperBound = monoIsotopicMassFromMZAndAdduct + tolerance/1000;
                } else { // PPM (Parts Per Million)
                    double tolerancePPM = mz * tolerance / 1_000_000.0d;
                    lowerBound = monoIsotopicMassFromMZAndAdduct - tolerancePPM;
                    upperBound = monoIsotopicMassFromMZAndAdduct + tolerancePPM;
                }

                final Integer compoundTypeFinal = compoundType;
                final double lowerBoundFinal = lowerBound;
                final double upperBoundFinal = upperBound;

                String sql = "SELECT c.* FROM compounds_view c WHERE ";
                sql += "c.mass BETWEEN " + lowerBoundFinal + " AND " + upperBoundFinal;

                if (metaboliteType == MetaboliteType.ONLYLIPIDS) {
                    sql += " AND c.compound_type = " + compoundTypeFinal;
                }

                String formulaTypeIntSql = "";
                if(formulaType != null && formulaType.isPresent()){
                    int formulaTypeInt = formulaType.get().getFormulaTypeIntValue();
                    formulaTypeIntSql = " AND c.formula_type_int = " + formulaTypeInt;
                }

                sql += formulaTypeIntSql;

                 if (!databaseConditions.isEmpty()) {
                    sql += " AND (" + String.join(" OR ", databaseConditions) + ")";
                }

                String finalSql = sql;

                Set<Compound> compounds = jdbcTemplate.query(
                        finalSql, rs -> {
                            while (rs.next()) {
                                CompoundDTO dto = CompoundMapper.fromResultSet(rs);
                                Compound compound = CompoundMapper.toCompound(dto);
                                compound.setPathways(fetchPathwaysForCompound(compound.getCompoundId()));
                                compoundsSet.add(compound);
                            }
                            return compoundsSet;
                        });

                logger.info("QUERY: {} ", finalSql);

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


    /**
     * This method obtains the pathways for a specific compound
     * @param compoundId the compound id of the compound to obtain its pathways
     * @return the pathways of a compound as a Set of pathways
     */
    private Set<Pathway> fetchPathwaysForCompound(int compoundId) {
        String sql = """
        SELECT p.* FROM pathways p
        INNER JOIN compounds_pathways cp ON cp.pathway_id = p.pathway_id
        WHERE cp.compound_id = ?
    """;

        List<Pathway> pathwayList = jdbcTemplate.query(sql, new Object[]{compoundId}, (rs, rowNum) -> {
            Pathway pathway = new Pathway();
            pathway.setPathwayId(rs.getInt("pathway_id"));
            pathway.setPathwayMap(rs.getString("pathway_map"));
            pathway.setPathwayName(rs.getString("pathway_name"));
            return pathway;
        });

        return new HashSet<>(pathwayList);
    }

}
