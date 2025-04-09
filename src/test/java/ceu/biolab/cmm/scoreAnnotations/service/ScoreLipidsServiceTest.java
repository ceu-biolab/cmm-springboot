package ceu.biolab.cmm.scoreAnnotations.service;

import ceu.biolab.cmm.scoreAnnotations.domain.Lipid;
import ceu.biolab.cmm.scoreAnnotations.domain.LipidScores;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import ceu.biolab.cmm.shared.domain.msFeature.Annotation;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotationsByAdduct;
import ceu.biolab.cmm.shared.domain.msFeature.ILCFeature;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ScoreLipidsServiceTest {
    
    @BeforeEach
    void setUp() {
    }
    
    @Test
    void testScoreEmptyInput() {
        // Test handling of empty input list
        List<AnnotatedFeature> features = new ArrayList<>();
        ScoreLipids.scoreLipidAnnotations(features, Optional.empty());
        
        assertTrue(features.isEmpty(), "Result should be empty");
    }
    
    @Test
    void testScoreSingleLipid() {
        // A single lipid should have no retention time score since there's nothing to compare with
        AnnotatedFeature feature = createEmptyAnnotatedFeature(800.5, 5.0);
        addRTLipidToAnnotations(feature, "PC", 36, 2);
        List<AnnotatedFeature> features = List.of(feature);
        
        ScoreLipids.scoreLipidAnnotations(features, Optional.empty());
        
        assertEquals(1, features.size(), "Should return one feature");
        assertEquals(1, features.get(0).getAnnotationsByAdducts().size(), "Should return one adduct");
        assertEquals(1, features.get(0).getAnnotationsByAdducts().get(0).getAnnotations().size(), "Should return one scored compound");
        // No scores for a single lipid as there's nothing to compare against
        Optional<Annotation> scoredCompound = findLipidAnnotation(features, "PC", 36, 2, 800.5, 5.0);
        assertTrue(scoredCompound.isPresent(), "Should find the scored compound");
        assertTrue(scoredCompound.get().getScores().size() == 1, "Should have one score");
        // Check that the present score is of type LipidScores
        assertTrue(scoredCompound.get().getScores().get(0) instanceof LipidScores, "Score should be of type LipidScores");
        // Check that the LipidScores object has no scores
        LipidScores lipidScores = (LipidScores) scoredCompound.get().getScores().get(0);
        assertTrue(lipidScores.getRtScoreMap().isEmpty(), "Should have no RT scores");
        assertFalse(lipidScores.getIonizationScore().isPresent(), "Should have no ionization score");
        assertFalse(lipidScores.getAdductRelationScore().isPresent(), "Should have no adduct score");
        assertFalse(lipidScores.getRtScore().isPresent(), "Should have no RT score");
    }
    
    @Test
    void testRuleSimple_LipidsRetentionTimeCarbons1_2() {
        // Test rule: "Lipids Retention Time Carbons 1 and 2"
        // Test with only two compounds
        // Same lipid type, same carbon count, fewer double bonds -> higher RT; higher double bonds -> lower RT
        // A true is added to the RT score list everytime this principle is satisfied

        AnnotatedFeature feature1 = createEmptyAnnotatedFeature(800.5, 6.0);
        addRTLipidToAnnotations(feature1, "PC", 36, 2);
        AnnotatedFeature feature2 = createEmptyAnnotatedFeature(800.5, 5.0);
        addRTLipidToAnnotations(feature2, "PC", 36, 4);
        List<AnnotatedFeature> features = List.of(feature1, feature2);

        ScoreLipids.scoreLipidAnnotations(features, Optional.empty());

        // Only 2 features and 2 compounds
        assertEquals(2, features.size(), "Should return two scored features");
        assertEquals(2, extractAnnotations(features).size(), "Should return two scored compounds");
        
        Optional<Annotation> pc36_4 = findLipidAnnotation(features, "PC", 36, 4, 800.5, 5.0);
        assertTrue(pc36_4.isPresent(), "Should find PC 36:4");
        Optional<Annotation> pc36_2 = findLipidAnnotation(features, "PC", 36, 2, 800.5, 6.0);
        assertTrue(pc36_2.isPresent(), "Should find PC 36:2");

        // Check PC 36:4 (rt=5) is scored correctly - should have lower RT than PC 36:2 (rt=6) 
        // Therefore should have one true score against the rt=6 feature (rule 1)
        assertFalse(pc36_4.get().getScores().isEmpty(), "PC 36:4 should have scores");
        // Check that the score is of type LipidScores
        assertTrue(pc36_4.get().getScores().get(0) instanceof LipidScores, "PC 36:4 should have a LipidScores score");
        LipidScores pc36_4_lipidScores = (LipidScores) pc36_4.get().getScores().get(0);
        assertFalse(pc36_4_lipidScores.getRtScoreMap().isEmpty(), "PC 36:4 should have the RT score map");
        Optional<List<Boolean>> pc36_4_compared = pc36_4_lipidScores.getRtScoresComparedTo(6.0, 800.5);
        assertTrue(pc36_4_compared.isPresent(), "PC 36:4 should have scores compared to PC 36:2");
        assertEquals(1, pc36_4_compared.get().size(), "PC 36:4 should have one score");
        assertTrue(pc36_4_compared.get().get(0), "PC 36:4 should have a true score against PC 36:2");
        
        // Check PC 36:2 (rt=6) is scored correctly - should have higher RT than PC 36:4 (rt=5)
        // Therefore should also have one true score against the rt=5 feature (rule 2)
        assertFalse(pc36_2.get().getScores().isEmpty(), "PC 36:2 should have scores");
        // Check that the score is of type LipidScores
        assertTrue(pc36_2.get().getScores().get(0) instanceof LipidScores, "PC 36:2 should have a LipidScores score");
        LipidScores pc36_2_lipidScores = (LipidScores) pc36_2.get().getScores().get(0);
        assertFalse(pc36_2_lipidScores.getRtScoreMap().isEmpty(), "PC 36:2 should have the RT score map");
        Optional<List<Boolean>> pc36_2_compared = pc36_2_lipidScores.getRtScoresComparedTo(5.0, 800.5);
        assertTrue(pc36_2_compared.isPresent(), "PC 36:2 should have scores compared to PC 36:4");
        assertEquals(1, pc36_2_compared.get().size(), "PC 36:2 should have one score");
        assertTrue(pc36_2_compared.get().get(0), "PC 36:2 should have a true score against PC 36:4");
    }

    @Test
    void testRuleMultiple_LipidsRetentionTimeCarbons1_2() {
        // Test with multiple compounds to verify retention time scoring with same carbons but different double bonds
        
        AnnotatedFeature feature1 = createEmptyAnnotatedFeature(800.5, 7.0);
        addRTLipidToAnnotations(feature1, "PC", 36, 1);
        Map<String, List<Boolean>> expectedScoresF1_36_1 = Map.of(
            "800.57.0", List.of(false),
            "804.55.0", List.of(true),
            "830.59.0", List.of(true, false)
        );
        addRTLipidToAnnotations(feature1, "PC", 36, 2);
        Map<String, List<Boolean>> expectedScoresF1_36_2 = Map.of(
            "800.57.0", List.of(false),
            "804.55.0", List.of(true),
            "830.59.0", List.of(true, false)
        );
        
        AnnotatedFeature feature2 = createEmptyAnnotatedFeature(804.5, 5.0);
        addRTLipidToAnnotations(feature2, "PC", 36, 3);
        Map<String, List<Boolean>> expectedScoresF2_36_3 = Map.of(
            "800.57.0", List.of(true, true),
            "830.59.0", List.of(true, false)
        );
        
        AnnotatedFeature feature3 = createEmptyAnnotatedFeature(830.5, 9.0);
        addRTLipidToAnnotations(feature3, "PE", 36, 5);
        Map<String, List<Boolean>> expectedScoresF3_36_5 = Map.of();
        addRTLipidToAnnotations(feature3, "PC", 36, 0);
        Map<String, List<Boolean>> expectedScoresF3_36_0 = Map.of(
            "800.57.0", List.of(true, true),
            "804.55.0", List.of(true),
            "830.59.0", List.of(false)
        );
        addRTLipidToAnnotations(feature3, "PC", 36, 8);
        Map<String, List<Boolean>> expectedScoresF3_36_8 = Map.of(
            "800.57.0", List.of(false, false),
            "804.55.0", List.of(false),
            "830.59.0", List.of(false)
        );
        
        List<AnnotatedFeature> features = List.of(feature1, feature2, feature3);
        
        ScoreLipids.scoreLipidAnnotations(features, Optional.empty());

        // Verify PC 36:1 scores
        Optional<Annotation> pc36_1 = findLipidAnnotation(features, "PC", 36, 1, 800.5, 7.0);
        assertTrue(pc36_1.isPresent(), "Should find PC 36:1");
        LipidScores pc36_1_scores = (LipidScores) pc36_1.get().getScores().get(0);
        verifyScores(pc36_1_scores, expectedScoresF1_36_1);
        
        // Verify PC 36:2 scores
        Optional<Annotation> pc36_2 = findLipidAnnotation(features, "PC", 36, 2, 800.5, 7.0);
        assertTrue(pc36_2.isPresent(), "Should find PC 36:2");
        LipidScores pc36_2_scores = (LipidScores) pc36_2.get().getScores().get(0);
        verifyScores(pc36_2_scores, expectedScoresF1_36_2);
        
        // Verify PC 36:3 scores
        Optional<Annotation> pc36_3 = findLipidAnnotation(features, "PC", 36, 3, 804.5, 5.0);
        assertTrue(pc36_3.isPresent(), "Should find PC 36:3");
        LipidScores pc36_3_scores = (LipidScores) pc36_3.get().getScores().get(0);
        verifyScores(pc36_3_scores, expectedScoresF2_36_3);
        
        // Verify PE 36:5 scores
        Optional<Annotation> pe36_5 = findLipidAnnotation(features, "PE", 36, 5, 830.5, 9.0);
        assertTrue(pe36_5.isPresent(), "Should find PE 36:5");
        LipidScores pe36_5_scores = (LipidScores) pe36_5.get().getScores().get(0);
        verifyScores(pe36_5_scores, expectedScoresF3_36_5);
        
        // Verify PC 36:0 scores
        Optional<Annotation> pc36_0 = findLipidAnnotation(features, "PC", 36, 0, 830.5, 9.0);
        assertTrue(pc36_0.isPresent(), "Should find PC 36:0");
        LipidScores pc36_0_scores = (LipidScores) pc36_0.get().getScores().get(0);
        verifyScores(pc36_0_scores, expectedScoresF3_36_0);
        
        // Verify PC 36:8 scores
        Optional<Annotation> pc36_8 = findLipidAnnotation(features, "PC", 36, 8, 830.5, 9.0);
        assertTrue(pc36_8.isPresent(), "Should find PC 36:8");
        LipidScores pc36_8_scores = (LipidScores) pc36_8.get().getScores().get(0);
        verifyScores(pc36_8_scores, expectedScoresF3_36_8);
    }

    @Test
    void testRuleSimple_AdductRelation() {
        // Test rule: "Adduct Relation"
        // Test with two compounds that should apply the rule and get a score
        AnnotatedFeature feature1 = createEmptyAnnotatedFeature(800.5, 0.85);
        addAdductLipidToAnnotations(feature1, 1, "M+Na");
        AnnotatedFeature feature2 = createEmptyAnnotatedFeature(800.5, 0.86);
        addAdductLipidToAnnotations(feature2, 1, "M+H");
        List<AnnotatedFeature> features = List.of(feature1, feature2);

        ScoreLipids.scoreLipidAnnotations(features, Optional.empty());
        
        // Verify both lipids received expected scores (ionization=1.0, adductRelation=1.0)
        assertEquals(2, extractAnnotations(features).size(), "Should have two annotations");
        
        // Get annotations for both features
        List<Annotation> annotations = extractAnnotations(features);
        for (Annotation annotation : annotations) {
            assertTrue(annotation.getScores().size() > 0, "Annotation should have scores");
            LipidScores lipidScores = (LipidScores) annotation.getScores().get(0);
            
            // Verify expected scores using our helper method
            verifyScores(
                lipidScores,
                1.0,  // expectedIonizationScore 
                1.0,  // expectedAdductRelationScore
                null  // expectedRtScore (not testing RT score here)
            );
            
            // Additional direct assertions
            assertTrue(lipidScores.getIonizationScore().isPresent(), "Should have ionization score");
            assertEquals(1.0, lipidScores.getIonizationScore().get(), "Ionization score should be 1.0");
            
            assertTrue(lipidScores.getAdductRelationScore().isPresent(), "Should have adduct relation score");
            assertEquals(1.0, lipidScores.getAdductRelationScore().get(), "Adduct relation score should be 1.0");
            
            // RT score map should be empty as this test focuses on adduct relations
            assertTrue(lipidScores.getRtScoreMap().isEmpty(), "RT score map should be empty");
        }
    }

    // Helper method to verify all available scores against expected values
    private void verifyScores(LipidScores lipidScores, 
                              Optional<Map<String, List<Boolean>>> expectedRtScores,
                              Optional<Double> expectedIonizationScore,
                              Optional<Double> expectedAdductRelationScore,
                              Optional<Double> expectedRtScore) {
        
        // Verify RT score map if expected values provided
        if (expectedRtScores.isPresent()) {
            Map<String, List<Boolean>> rtScoresMap = expectedRtScores.get();
            // If no expected scores, verify that RT score map is empty
            if (rtScoresMap.isEmpty()) {
                assertTrue(lipidScores.getRtScoreMap().isEmpty(), "Should have no RT scores");
            } else {
                // For each expected score entry, verify the actual scores match
                for (Map.Entry<String, List<Boolean>> entry : rtScoresMap.entrySet()) {
                    String key = entry.getKey();
                    List<Boolean> expectedRtScoresList = entry.getValue();
                    
                    // Parse the key to get RT and MZ
                    double mz = Double.parseDouble(key.substring(0, 5));
                    double rt = Double.parseDouble(key.substring(5));
                    
                    // Get scores for the specific feature comparison
                    Optional<List<Boolean>> actualScores = lipidScores.getRtScoresComparedTo(rt, mz);
                    assertTrue(actualScores.isPresent(), "Should have scores for comparison to " + key);
                    
                    List<Boolean> actualRtScores = actualScores.get();
                    assertEquals(expectedRtScoresList.size(), actualRtScores.size(), 
                            "Should have the expected number of scores for " + key);
                    
                    // Compare each score
                    for (int i = 0; i < expectedRtScoresList.size(); i++) {
                        assertEquals(expectedRtScoresList.get(i), actualRtScores.get(i),
                                "Score at index " + i + " for " + key + " should match");
                    }
                }
            }
        }
        
        // Verify ionization score if expected value provided
        if (expectedIonizationScore.isPresent()) {
            assertEquals(expectedIonizationScore, lipidScores.getIonizationScore(), 
                    "Ionization score should match expected value");
        }
        
        // Verify adduct relation score if expected value provided
        if (expectedAdductRelationScore.isPresent()) {
            assertEquals(expectedAdductRelationScore, lipidScores.getAdductRelationScore(), 
                    "Adduct relation score should match expected value");
        }
        
        // Verify RT score if expected value provided
        if (expectedRtScore.isPresent()) {
            assertEquals(expectedRtScore, lipidScores.getRtScore(), 
                    "RT score should match expected value");
        }
    }
    
    // Convenience overloads
    private void verifyScores(LipidScores lipidScores, Map<String, List<Boolean>> expectedRtScores) {
        verifyScores(lipidScores, Optional.of(expectedRtScores), Optional.empty(), Optional.empty(), Optional.empty());
    }
    
    private void verifyScores(LipidScores lipidScores, 
                              Optional<Double> expectedIonizationScore,
                              Optional<Double> expectedAdductRelationScore,
                              Optional<Double> expectedRtScore) {
        verifyScores(lipidScores, Optional.empty(), expectedIonizationScore, expectedAdductRelationScore, expectedRtScore);
    }

    // New overload for verifying only specific scores without RT map
    private void verifyScores(LipidScores lipidScores, 
                              Double expectedIonizationScore,
                              Double expectedAdductRelationScore,
                              Double expectedRtScore) {
        verifyScores(
            lipidScores, 
            Optional.empty(),
            expectedIonizationScore != null ? Optional.of(expectedIonizationScore) : Optional.empty(),
            expectedAdductRelationScore != null ? Optional.of(expectedAdductRelationScore) : Optional.empty(),
            expectedRtScore != null ? Optional.of(expectedRtScore) : Optional.empty()
        );
    }

    // Helper methods 
    
    private AnnotatedFeature createEmptyAnnotatedFeature(double mz, double rt) {
        return new AnnotatedFeature(rt, mz);
    }

    private void addRTLipidToAnnotations(AnnotatedFeature feature, String lipidType, int carbons, int doubleBonds) {
        // Create Lipid with builder
        Lipid lipid = Lipid.builder()
            .lipidType(lipidType)
            .numberChains(2)
            .numberCarbons(carbons)
            .numberDoubleBonds(doubleBonds)
            .build();
        feature.addCompoundForAdduct("TEST", lipid);
    }

    /**
     * Adds a lipid annotation with the specified compound ID and adduct
     * @param feature The feature to annotate
     * @param compoundId The compound ID to add
     * @param adduct The adduct to use (e.g. "M+H", "M+Na")
     */
    private void addAdductLipidToAnnotations(AnnotatedFeature feature, int compoundId, String adduct) {
        Lipid lipid = Lipid.builder()
            .compoundId(compoundId)
            .build();
        feature.addCompoundForAdduct(adduct, lipid);
    }
    
    /**
     * Adds a lipid annotation with the specified compound ID using "TEST" as the default adduct
     * @param feature The feature to annotate
     * @param compoundId The compound ID to add
     */
    private void addAdductLipidToAnnotations(AnnotatedFeature feature, int compoundId) {
        addAdductLipidToAnnotations(feature, compoundId, "TEST");
    }

    private Optional<Annotation> findLipidAnnotation(List<AnnotatedFeature> features, String lipidType, int carbons, int doubleBonds, double mz, double rt) {
        for (AnnotatedFeature feature : features) {
            for (AnnotationsByAdduct scoredAnnotationsByAdduct : feature.getAnnotationsByAdducts()) {
                for (Annotation scoredCompound : scoredAnnotationsByAdduct.getAnnotations()) {
                    if (scoredCompound.getCompound() instanceof Lipid lipid &&
                        feature.getFeature() instanceof ILCFeature lcFeature &&
                        lipid.getLipidType().equals(lipidType) &&
                        lipid.getNumberCarbons() == carbons &&
                        lipid.getNumberDoubleBonds() == doubleBonds &&
                        lcFeature.getRtValue() == rt &&
                        lcFeature.getMzValue() == mz) {
                        return Optional.of(scoredCompound);
                    }
                }
            }
        }
        return Optional.empty();
    }

    // Extract all scored compounds from a list of scored features
    private List<Annotation> extractAnnotations(List<AnnotatedFeature> features) {
        List<Annotation> scoredCompounds = new ArrayList<>();
        for (AnnotatedFeature feature : features) {
            for (AnnotationsByAdduct scoredAnnotationsByAdduct : feature.getAnnotationsByAdducts()) {
                scoredCompounds.addAll(scoredAnnotationsByAdduct.getAnnotations());
            }
        }
        return scoredCompounds;
    }
}
