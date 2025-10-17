package ceu.biolab.cmm.msSearch.repository;

import ceu.biolab.*;
import ceu.biolab.cmm.msSearch.dto.CompoundDTO;
import ceu.biolab.cmm.msSearch.domain.compound.CompoundMapper;

import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.shared.domain.adduct.AdductDefinition;
import ceu.biolab.cmm.shared.service.adduct.AdductService;
import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.compound.CompoundType;
import ceu.biolab.cmm.shared.domain.compound.Pathway;
import ceu.biolab.cmm.shared.domain.msFeature.*;
import com.apicatalog.jsonld.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Repository
public class CompoundRepository {

    private static final Logger logger = LoggerFactory.getLogger(CompoundRepository.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ResourceLoader resourceLoader;

    private String msSearchQueryTemplate;

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
        logger.info("request: {}", detectedAdduct);
        CompoundType compoundType = null;

        if (mz == null || tolerance == null || mzToleranceMode == null || ionizationMode == null) {
            return annotatedMSFeature;
        }

        if (metaboliteType == MetaboliteType.ONLYLIPIDS) {
            compoundType = CompoundType.LIPID;
        }

        Optional<Set<String>> allowedElements = resolveAllowedElements(formulaType);

        double lowerBound, upperBound;

        //TODO solo puede ser positivo: verificar : crear clase PositiveDouble en el constructor final si es menor que 0 ERROR

        try {
            IMSFeature msFeature = new MSFeature(mz, 0.0);
            AnnotatedFeature annotatedFeature = new AnnotatedFeature(msFeature);
            Map<String, AdductDefinition> adductsToProcess = new LinkedHashMap<>();

            logger.info("detected adduct: {}", detectedAdduct);
            logger.info(" adductS: {}", adductsString);

            if (detectedAdduct != null && detectedAdduct.isPresent() && !detectedAdduct.isEmpty() && StringUtils.isNotBlank(detectedAdduct.get())) {
                AdductDefinition detectedDefinition = AdductService.requireDefinition(ionizationMode, detectedAdduct.get().trim());
                adductsToProcess.putIfAbsent(detectedDefinition.canonical(), detectedDefinition);
            } else {
                for (String adduct : adductsString) {
                    AdductDefinition definition = AdductService.requireDefinition(ionizationMode, adduct);
                    adductsToProcess.putIfAbsent(definition.canonical(), definition);
                }
            }

            logger.info(" adductS process: {}", adductsToProcess);
            if (adductsToProcess == null || adductsToProcess.isEmpty()) {
                return annotatedMSFeature;
            }

            List<AdductDefinition> orderedAdducts = AdductService.sortByPriority(
                    new LinkedHashSet<>(adductsToProcess.values()), ionizationMode);
            logger.info("ordered adducts: {}", orderedAdducts.stream().map(AdductDefinition::canonical).toList());

            for (AdductDefinition adductDefinition : orderedAdducts) {
                String adductString = adductDefinition.canonical();

                Set<Compound> compoundsSet = new HashSet<>();

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

                List<String> databaseConditions = Database.databaseConditions(databases);

                double monoIsotopicMassFromMZAndAdduct = AdductService.neutralMassFromMz(mz, adductDefinition);

                // Calculate tolerance range based on PPM or DA
                if (mzToleranceMode == MzToleranceMode.MDA) {
                    lowerBound = monoIsotopicMassFromMZAndAdduct - tolerance/1000;
                    upperBound = monoIsotopicMassFromMZAndAdduct + tolerance/1000;
                } else { // PPM (Parts Per Million)
                    double tolerancePPM = mz * tolerance / 1_000_000.0d;
                    lowerBound = monoIsotopicMassFromMZAndAdduct - tolerancePPM;
                    upperBound = monoIsotopicMassFromMZAndAdduct + tolerancePPM;
                }

                final CompoundType compoundTypeFinal = compoundType;
                final double lowerBoundFinal = lowerBound;
                final double upperBoundFinal = upperBound;

                String sqlTemplate = loadMsSearchQueryTemplate();
                String compoundTypeClause = compoundTypeFinal != null ? " AND cv.compound_type = " + compoundTypeFinal.getDbValue() : "";

                String databaseClause = "";
                if (!databaseConditions.isEmpty()) {
                    List<String> normalizedConditions = databaseConditions.stream()
                            .map(condition -> condition.replace("c.", "cv."))
                            .collect(Collectors.toList());
                    databaseClause = " AND (" + String.join(" OR ", normalizedConditions) + ")";
                }

                String finalSql = sqlTemplate
                        .replace("(:lowerBound)", String.valueOf(lowerBoundFinal))
                        .replace("(:upperBound)", String.valueOf(upperBoundFinal))
                        .replace("(:compoundTypeFilter)", compoundTypeClause)
                        .replace("(:databaseFilterCondition)", databaseClause);

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

                Set<Compound> filteredCompounds = compounds.stream()
                        .peek(this::normalizeLipidMapsClassification)
                        .filter(comp -> matchesRequestedAlphabet(comp, allowedElements))
                        .collect(Collectors.toCollection(LinkedHashSet::new));

                List<Annotation> annotations = new ArrayList<>();
                for (Compound compound : filteredCompounds) {
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

    private Optional<Set<String>> resolveAllowedElements(Optional<FormulaType> formulaType) {
        if (formulaType == null || formulaType.isEmpty()) {
            return Optional.empty();
        }
        FormulaType requested = formulaType.get();
        if (requested == FormulaType.ALL || requested == FormulaType.ALLD) {
            return Optional.empty();
        }
        return Optional.of(parseAlphabetToElements(requested.name()));
    }

    private boolean matchesRequestedAlphabet(Compound compound, Optional<Set<String>> allowedElements) {
        if (allowedElements == null || allowedElements.isEmpty()) {
            return true;
        }
        Optional<Set<String>> compoundElements = compound.formulaElements();
        if (compoundElements.isEmpty()) {
            // Compounds without a formula should be included for all requested alphabets.
            return true;
        }
        return allowedElements.get().containsAll(compoundElements.get());
    }

    private Set<String> parseAlphabetToElements(String alphabet) {
        Set<String> elements = new LinkedHashSet<>();
        if (alphabet == null) {
            return elements;
        }
        Matcher matcher = ALPHABET_PATTERN.matcher(alphabet.toUpperCase());
        while (matcher.find()) {
            elements.add(matcher.group(1));
        }
        return elements;
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
    private String extractCode(String value) {
        if (value == null) {
            return "";
        }
        Matcher matcher = BRACKET_CODE_PATTERN.matcher(value);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return value;
    }

    private void normalizeLipidMapsClassification(Compound compound) {
        if (compound == null || compound.getLipidMapsClassifications() == null) {
            return;
        }
        for (ceu.biolab.cmm.msSearch.domain.compound.LipidMapsClassification classification : compound.getLipidMapsClassifications()) {
            if (classification == null) {
                continue;
            }
            classification.setCategory(extractCode(classification.getCategory()));
            classification.setMainClass(extractCode(classification.getMainClass()));
            classification.setSubClass(extractCode(classification.getSubClass()));
            classification.setClassLevel4(extractCode(classification.getClassLevel4()));
        }
    }

    private static final Pattern ALPHABET_PATTERN = Pattern.compile("([A-Z][a-z]?)");
    private static final Pattern BRACKET_CODE_PATTERN = Pattern.compile(".*\\[(.+?)\\].*");

    private String loadMsSearchQueryTemplate() {
        if (msSearchQueryTemplate == null) {
            Resource resource = resourceLoader.getResource("classpath:sql/msSearch/compound_window_search.sql");
            try (InputStream is = resource.getInputStream()) {
                msSearchQueryTemplate = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to load MS search SQL template", e);
            }
        }
        return msSearchQueryTemplate;
    }
}
