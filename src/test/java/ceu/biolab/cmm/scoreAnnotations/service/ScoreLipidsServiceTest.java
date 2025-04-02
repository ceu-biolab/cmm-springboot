package ceu.biolab.cmm.scoreAnnotations.service;

import ceu.biolab.cmm.scoreAnnotations.dto.ScoredAnnotatedRTFeature;
import ceu.biolab.cmm.scoreAnnotations.dto.ScoredAnnotationsByAdduct;
import ceu.biolab.cmm.scoreAnnotations.dto.ScoredCompound;
import ceu.biolab.cmm.scoreAnnotations.model.Lipid;
import ceu.biolab.cmm.shared.domain.AnnotatedRTFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        List<ScoredAnnotatedRTFeature> result = ScoreLipids.scoreLipids(List.of());
        
        assertTrue(result.isEmpty(), "Result should be empty for empty input");
    }
    
    @Test
    void testScoreSingleLipid() {
        // A single lipid should have no retention time score since there's nothing to compare with
        AnnotatedRTFeature feature = createEmptyAnnotatedFeature(800.5, 5.0);
        addLipidToAnnotations(feature, "PC", 36, 2);
        List<AnnotatedRTFeature> features = List.of(feature);
        
        List<ScoredAnnotatedRTFeature> result = ScoreLipids.scoreLipids(features);
        
        assertEquals(1, result.size(), "Should return one feature");
        assertEquals(1, result.get(0).getScoredAnnotationsByAdducts().size(), "Should return one adduct");
        assertEquals(1, result.get(0).getScoredAnnotationsByAdducts().get(0).getAnnotations().size(), "Should return one scored compound");
        // No scores for a single lipid as there's nothing to compare against
        Optional<ScoredCompound> scoredCompound = findScoredLipid(result, "PC", 36, 2, 800.5, 5.0);
        System.out.println(result);
        assertTrue(scoredCompound.isPresent(), "Should find the scored compound");
        assertTrue(scoredCompound.get().getScores().isPresent(), "Scores should be present, even if with no matches");
        assertTrue(scoredCompound.get().getScores().get().getRtScoreMap().isEmpty(), "The rtScoreMap should be empty for a single lipid");
    }
    
    @Test
    void testRuleSimple_LipidsRetentionTimeCarbons1_2() {
        // Test rule: "Lipids Retention Time Carbons 1 and 2"
        // Test with only two compounds
        // Same lipid type, same carbon count, fewer double bonds -> higher RT; higher double bonds -> lower RT
        // A true is added to the RT score list everytime this principle is satisfied

        AnnotatedRTFeature feature1 = createEmptyAnnotatedFeature(800.5, 6.0);
        addLipidToAnnotations(feature1, "PC", 36, 2);
        AnnotatedRTFeature feature2 = createEmptyAnnotatedFeature(800.5, 5.0);
        addLipidToAnnotations(feature2, "PC", 36, 4);
        List<AnnotatedRTFeature> features = List.of(feature1, feature2);

        List<ScoredAnnotatedRTFeature> result = ScoreLipids.scoreLipids(features);

        // Only 2 features and 2 compounds
        assertEquals(2, result.size(), "Should return two scored features");
        assertEquals(2, extractScoredCompounds(result).size(), "Should return two scored compounds");
        
        Optional<ScoredCompound> pc36_4 = findScoredLipid(result, "PC", 36, 4, 800.5, 5.0);
        assertTrue(pc36_4.isPresent(), "Should find PC 36:4");
        Optional<ScoredCompound> pc36_2 = findScoredLipid(result, "PC", 36, 2, 800.5, 6.0);
        assertTrue(pc36_2.isPresent(), "Should find PC 36:2");

        // Check PC 36:4 (rt=5) is scored correctly - should have lower RT than PC 36:2 (rt=6) 
        // Therefore should have one true score against the rt=6 feature (rule 1)
        assertTrue(pc36_4.get().getScores().isPresent(), "PC 36:4 should have scores");
        Optional<List<Boolean>> pc36_4_compared = pc36_4.get().getRtScoresComparedTo(6.0, 800.5);
        assertTrue(pc36_4_compared.isPresent(), "PC 36:4 should have scores compared to PC 36:2");
        assertEquals(1, pc36_4_compared.get().size(), "PC 36:4 should have one score");
        assertTrue(pc36_4_compared.get().get(0), "PC 36:4 should have a true score against PC 36:2");
        
        // Check PC 36:2 (rt=6) is scored correctly - should have higher RT than PC 36:4 (rt=5)
        // Therefore should also have one true score against the rt=5 feature (rule 2)
        assertTrue(pc36_2.get().getScores().isPresent(), "PC 36:2 should have scores");
        Optional<List<Boolean>> pc36_2_compared = pc36_2.get().getRtScoresComparedTo(5.0, 800.5);
        assertTrue(pc36_2_compared.isPresent(), "PC 36:2 should have scores compared to PC 36:4");
        assertEquals(1, pc36_2_compared.get().size(), "PC 36:2 should have one score");
        assertTrue(pc36_2_compared.get().get(0), "PC 36:2 should have a false true against PC 36:4");
    }

    // Helper methods 
    
    private AnnotatedRTFeature createEmptyAnnotatedFeature(double mz, double rt) {
        return new AnnotatedRTFeature(rt, mz);
    }

    private void addLipidToAnnotations(AnnotatedRTFeature feature, String lipidType, int carbons, int doubleBonds) {
        // Create Lipid with builder
        Lipid lipid = Lipid.builder()
            .lipidType(lipidType)
            .numberChains(2)
            .numberCarbons(carbons)
            .numberDoubleBonds(doubleBonds)
            .build();
        feature.addCompoundForAdduct("TEST", lipid);
    }

    private Optional<ScoredCompound> findScoredLipid(List<ScoredAnnotatedRTFeature> scoredFeatures, String lipidType, int carbons, int doubleBonds, double mz, double rt) {
        for (ScoredAnnotatedRTFeature scoredFeature : scoredFeatures) {
            for (ScoredAnnotationsByAdduct scoredAnnotationsByAdduct : scoredFeature.getScoredAnnotationsByAdducts()) {
                for (ScoredCompound scoredCompound : scoredAnnotationsByAdduct.getAnnotations()) {
                    System.out.println(scoredCompound);
                    if (scoredCompound.getCompound() instanceof Lipid lipid &&
                        lipid.getLipidType().equals(lipidType) &&
                        lipid.getNumberCarbons() == carbons &&
                        lipid.getNumberDoubleBonds() == doubleBonds &&
                        scoredFeature.getRtValue() == rt &&
                        scoredFeature.getMzValue() == mz) {
                        return Optional.of(scoredCompound);
                    }
                }
            }
        }
        return Optional.empty();
    }

    // Extract all scored compounds from a list of scored features
    private List<ScoredCompound> extractScoredCompounds(List<ScoredAnnotatedRTFeature> scoredFeatures) {
        List<ScoredCompound> scoredCompounds = new ArrayList<>();
        for (ScoredAnnotatedRTFeature scoredFeature : scoredFeatures) {
            for (ScoredAnnotationsByAdduct scoredAnnotationsByAdduct : scoredFeature.getScoredAnnotationsByAdducts()) {
                scoredCompounds.addAll(scoredAnnotationsByAdduct.getAnnotations());
            }
        }
        return scoredCompounds;
    }
}
