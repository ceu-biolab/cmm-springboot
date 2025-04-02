package ceu.biolab.cmm.scoreAnnotations.service;

import ceu.biolab.cmm.scoreAnnotations.model.Lipid;
import ceu.biolab.cmm.scoreAnnotations.model.LipidScores;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import ceu.biolab.cmm.shared.domain.msFeature.Annotation;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotationsByAdduct;
import ceu.biolab.cmm.shared.domain.msFeature.ILCFeature;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Notation;

import java.util.ArrayList;
import java.util.List;
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
