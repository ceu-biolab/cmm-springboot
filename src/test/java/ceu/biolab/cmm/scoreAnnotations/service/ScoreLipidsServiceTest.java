package ceu.biolab.cmm.scoreAnnotations.service;

import ceu.biolab.cmm.scoreAnnotations.model.Lipid;
import ceu.biolab.cmm.scoreAnnotations.model.LipidScores;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import ceu.biolab.cmm.shared.domain.msFeature.Annotation;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotationsByAdduct;
import ceu.biolab.cmm.shared.domain.msFeature.ILCMSFeature;

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
        ScoreLipids.scoreLipidAnnotations(features);
        
        assertTrue(features.isEmpty(), "Result should be empty");
    }
    
    @Test
    void testScoreSingleLipid() {
        // A single lipid should have no retention time score since there's nothing to compare with
        AnnotatedFeature feature = createEmptyAnnotatedFeature(800.5, 5.0);
        addLipidToAnnotations(feature, "PC", 36, 2);
        List<AnnotatedFeature> features = List.of(feature);
        
        ScoreLipids.scoreLipidAnnotations(features);
        
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
        assertFalse(lipidScores.getAdductScore().isPresent(), "Should have no adduct score");
        assertFalse(lipidScores.getRtScore().isPresent(), "Should have no RT score");
    }
    
    @Test
    void testRuleSimple_LipidsRetentionTimeCarbons1_2() {
        // Test rule: "Lipids Retention Time Carbons 1 and 2"
        // Test with only two compounds
        // Same lipid type, same carbon count, fewer double bonds -> higher RT; higher double bonds -> lower RT
        // A true is added to the RT score list everytime this principle is satisfied

        AnnotatedFeature feature1 = createEmptyAnnotatedFeature(800.5, 6.0);
        addLipidToAnnotations(feature1, "PC", 36, 2);
        AnnotatedFeature feature2 = createEmptyAnnotatedFeature(800.5, 5.0);
        addLipidToAnnotations(feature2, "PC", 36, 4);
        List<AnnotatedFeature> features = List.of(feature1, feature2);

        ScoreLipids.scoreLipidAnnotations(features);

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
        addLipidToAnnotations(feature1, "PC", 36, 1);
        Map<String, List<Boolean>> expectedScoresF1_36_1 = Map.of(
            "800.57.0", List.of(false),
            "804.55.0", List.of(true),
            "830.59.0", List.of(true, false)
        );
        addLipidToAnnotations(feature1, "PC", 36, 2);
        Map<String, List<Boolean>> expectedScoresF1_36_2 = Map.of(
            "800.57.0", List.of(false),
            "804.55.0", List.of(true),
            "830.59.0", List.of(true, false)
        );
        
        AnnotatedFeature feature2 = createEmptyAnnotatedFeature(804.5, 5.0);
        addLipidToAnnotations(feature2, "PC", 36, 3);
        Map<String, List<Boolean>> expectedScoresF2_36_3 = Map.of(
            "800.57.0", List.of(true, true),
            "830.59.0", List.of(true, false)
        );
        
        AnnotatedFeature feature3 = createEmptyAnnotatedFeature(830.5, 9.0);
        addLipidToAnnotations(feature3, "PE", 36, 5);
        Map<String, List<Boolean>> expectedScoresF3_36_5 = Map.of();
        addLipidToAnnotations(feature3, "PC", 36, 0);
        Map<String, List<Boolean>> expectedScoresF3_36_0 = Map.of(
            "800.57.0", List.of(true, true),
            "804.55.0", List.of(true),
            "830.59.0", List.of(false)
        );
        addLipidToAnnotations(feature3, "PC", 36, 8);
        Map<String, List<Boolean>> expectedScoresF3_36_8 = Map.of(
            "800.57.0", List.of(false, false),
            "804.55.0", List.of(false),
            "830.59.0", List.of(false)
        );
        
        List<AnnotatedFeature> features = List.of(feature1, feature2, feature3);
        
        ScoreLipids.scoreLipidAnnotations(features);

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

    // Helper method to verify scores against expected values
    private void verifyScores(LipidScores lipidScores, Map<String, List<Boolean>> expectedScores) {
        // If no expected scores, verify that RT score map is empty
        if (expectedScores.isEmpty()) {
            assertTrue(lipidScores.getRtScoreMap().isEmpty(), "Should have no RT scores");
            return;
        }
        
        // For each expected score entry, verify the actual scores match
        for (Map.Entry<String, List<Boolean>> entry : expectedScores.entrySet()) {
            String key = entry.getKey();
            List<Boolean> expectedRtScores = entry.getValue();
            
            // Parse the key to get RT and MZ
            double mz = Double.parseDouble(key.substring(0, 5));
            double rt = Double.parseDouble(key.substring(5));
            
            // Get scores for the specific feature comparison
            Optional<List<Boolean>> actualScores = lipidScores.getRtScoresComparedTo(rt, mz);
            assertTrue(actualScores.isPresent(), "Should have scores for comparison to " + key);
            
            List<Boolean> actualRtScores = actualScores.get();
            assertEquals(expectedRtScores.size(), actualRtScores.size(), 
                    "Should have the expected number of scores for " + key);
            
            // Compare each score
            for (int i = 0; i < expectedRtScores.size(); i++) {
                assertEquals(expectedRtScores.get(i), actualRtScores.get(i),
                        "Score at index " + i + " for " + key + " should match");
            }
        }
    }

    // Helper methods 
    
    private AnnotatedFeature createEmptyAnnotatedFeature(double mz, double rt) {
        return new AnnotatedFeature(rt, mz);
    }

    private void addLipidToAnnotations(AnnotatedFeature feature, String lipidType, int carbons, int doubleBonds) {
        // Create Lipid with builder
        Lipid lipid = Lipid.builder()
            .lipidType(lipidType)
            .numberChains(2)
            .numberCarbons(carbons)
            .numberDoubleBonds(doubleBonds)
            .build();
        feature.addCompoundForAdduct("TEST", lipid);
    }

    private Optional<Annotation> findLipidAnnotation(List<AnnotatedFeature> features, String lipidType, int carbons, int doubleBonds, double mz, double rt) {
        for (AnnotatedFeature feature : features) {
            for (AnnotationsByAdduct scoredAnnotationsByAdduct : feature.getAnnotationsByAdducts()) {
                for (Annotation scoredCompound : scoredAnnotationsByAdduct.getAnnotations()) {
                    if (scoredCompound.getCompound() instanceof Lipid lipid &&
                        feature.getFeature() instanceof ILCMSFeature lcFeature &&
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
