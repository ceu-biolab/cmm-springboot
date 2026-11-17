package ceu.biolab.cmm.unit.scoreAnnotations.service;

import ceu.biolab.cmm.shared.domain.compound.LipidMapsClassification;
import ceu.biolab.cmm.scoreAnnotations.domain.CompoundScores;
import ceu.biolab.cmm.scoreAnnotations.domain.Lipid;
import ceu.biolab.cmm.scoreAnnotations.service.ScoreAnnotationsService;
import ceu.biolab.cmm.shared.domain.ExperimentParameters;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.ModifierType;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.compound.CompoundType;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import ceu.biolab.cmm.shared.domain.msFeature.LCMSFeature;
import ceu.biolab.cmm.shared.service.adduct.AdductService;
import org.drools.decisiontable.InputType;
import org.drools.decisiontable.SpreadsheetCompiler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class ScoringCompatibilityTest {
    @Test
    void alternativeAnnotationsOfOneFeatureDoNotProvideRtEvidence() {
        var feature = feature(800, 5, "[M+H]+", lipid(1, "PC", 2, 36, 2));
        feature.addCompoundForAdduct("[M+H]+", lipid(2, "PC", 2, 36, 4));

        ScoreAnnotationsService.scoreAnnotations(List.of(feature), Optional.empty());

        for (var annotation : feature.getAnnotationsByAdducts().getFirst().getAnnotations()) {
            var score = (CompoundScores) annotation.getScores().getFirst();
            assertTrue(score.getRtScore().isEmpty());
            assertTrue(score.getFinalScore().isEmpty());
            assertEquals(0, score.getTotalNumberRtScores());
        }
    }

    @Test
    void identicalCoordinatesInSeparateFeatureEntriesDoNotProvideRtEvidence() {
        var first = feature(800, 5, "[M+H]+", lipid(1, "PC", 2, 36, 2));
        var second = feature(800, 5, "[M+Na]+", lipid(2, "PC", 2, 36, 4));
        ScoreAnnotationsService.scoreAnnotations(List.of(first, second), Optional.empty());
        assertTrue(scores(first).getRtScore().isEmpty());
        assertTrue(scores(second).getRtScore().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"chains", "carbons", "doubleBonds", "lipidType"})
    void incompleteStructuralMetadataDoesNotCreateRtEvidence(String missing) {
        var incomplete = lipid(1, "lipidType".equals(missing) ? null : "PC",
                "chains".equals(missing) ? null : 2,
                "carbons".equals(missing) ? null : 36,
                "doubleBonds".equals(missing) ? null : 2);
        var first = feature(800, 6, "[M+H]+", incomplete);
        var second = feature(802, 5, "[M+H]+", lipid(2, "PC", 2, 36, 4));

        ScoreAnnotationsService.scoreAnnotations(List.of(first, second), Optional.empty());

        assertTrue(scores(first).getRtScore().isEmpty());
        assertTrue(scores(second).getRtScore().isEmpty());
        var wrapped = new Lipid(incomplete);
        assertEquals(incomplete.getDoubleBonds(), wrapped.getNumberDoubleBonds());
        assertEquals(incomplete.getNumChains(), wrapped.getNumberChains());
        assertEquals(incomplete.getNumCarbons(), wrapped.getNumberCarbons());
    }

    @Test
    void knownZeroDoubleBondsStillProvideRtEvidence() {
        var saturated = feature(800, 6, "[M+H]+", lipid(1, "PC", 2, 36, 0));
        var unsaturated = feature(802, 5, "[M+H]+", lipid(2, "PC", 2, 36, 2));
        ScoreAnnotationsService.scoreAnnotations(List.of(saturated, unsaturated), Optional.empty());
        assertEquals(Optional.of(1.0), scores(saturated).getRtScore());
        assertEquals(Optional.of(1.0), scores(unsaturated).getRtScore());
    }

    @Test
    void ionizationAdjustmentsRequireAnExistingBaseline() {
        var score = new CompoundScores();
        score.setIonizationScore(-2);
        assertTrue(score.getIonizationScore().isEmpty());
        score.setIonizationScore(-3);
        assertTrue(score.getIonizationScore().isEmpty());
        score.setIonizationScore(0);
        score.setIonizationScore(-2);
        assertEquals(Optional.of(1.0), score.getIonizationScore());
        score.setIonizationScore(-3);
        assertEquals(Optional.of(0.1), score.getIonizationScore());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void supportingAdductsAdjustIonizationAfterBaselineRules(boolean reverse) {
        var compound = classifiedLipid("GP0101");
        var sodium = feature(822, 5, "[M+Na]+", compound);
        var proton = feature(800, 5.01, "[M+H]+", compound);
        ScoreAnnotationsService.scoreAnnotations(reverse ? List.of(proton, sodium) : List.of(sodium, proton),
                Optional.of(parameters(IonizationMode.POSITIVE, ModifierType.NONE)));
        for (var feature : List.of(sodium, proton)) {
            assertEquals(Optional.of(1.0), scores(feature).getIonizationScore());
            assertEquals(Optional.of(1.0), scores(feature).getAdductRelationScore());
            assertEquals(Optional.of(1.0), scores(feature).getFinalScore());
        }
    }

    @ParameterizedTest
    @CsvSource({
            "FA01, [M-H]-, NONE, 1.0",
            "FA01, [M-H-H2O]-, NONE, 0.5",
            "GP0101, [M+Cl]-, NONE, 0.5",
            "GP0101, [M+FA-H]-, HCOO, 1.0",
            "GP0101, [M+Hac-H]-, CH3COO, 1.0",
            "GP0101, [M-H]-, NONE, 0.0",
            "GP0201, [M-H]-, NONE, 1.0",
            "GP0601, [M-H]-, NONE, 1.0",
            "SP03, [M-H]-, NONE, 0.0"
    })
    void negativeIonizationRulesUseCanonicalAdducts(String classification, String adduct,
                                                   ModifierType modifier, double expected) {
        var feature = feature(800, 5, adduct, classifiedLipid(classification));
        ScoreAnnotationsService.scoreAnnotations(List.of(feature),
                Optional.of(parameters(IonizationMode.NEGATIVE, modifier)));
        assertEquals(Optional.of(expected), scores(feature).getIonizationScore());
        assertEquals(expected, scores(feature).getFinalScore().orElseThrow(), 1e-12);
    }

    @Test
    void gp03DehydratedProtonatedIonUsesTheConfirmedPositiveRule() {
        var feature = feature(800, 5, "[M+H-H2O]+", classifiedLipid("GP0301"));
        ScoreAnnotationsService.scoreAnnotations(List.of(feature),
                Optional.of(parameters(IonizationMode.POSITIVE, ModifierType.NONE)));
        assertEquals(Optional.of(0.0), scores(feature).getIonizationScore());
    }

    @Test
    void everyIonizationRuleAdductExistsInTheCatalogForItsPolarity() throws Exception {
        String resource = "/ceu/biolab/cmm/scoreAnnotations/model/ScoreLipidsRules.drl.xlsx";
        String drl;
        try (var input = getClass().getResourceAsStream(resource)) {
            assertNotNull(input);
            drl = new SpreadsheetCompiler().compile(input, InputType.XLS);
        }
        var rules = Pattern.compile("rule \\\"IonizationLipids_[^\\\"]+\\\".*?\\nend", Pattern.DOTALL).matcher(drl);
        int rulesChecked = 0;
        while (rules.find()) {
            String rule = rules.group();
            assertTrue(rule.contains("agenda-group \"ionization\""), rule);
            var adduct = Pattern.compile("getAdduct\\(\\) == \\\"([^\\\"]+)\\\"").matcher(rule);
            if (adduct.find()) {
                var mode = rule.contains("getIonModeStr() == \"negative\"")
                        ? IonizationMode.NEGATIVE : IonizationMode.POSITIVE;
                assertDoesNotThrow(() -> AdductService.requireDefinition(mode, adduct.group(1)), rule);
            }
            rulesChecked++;
        }
        assertEquals(111, rulesChecked);
    }

    private static Compound lipid(int id, String type, Integer chains, Integer carbons, Integer doubleBonds) {
        return Compound.builder().compoundId(id).compoundType(CompoundType.LIPID).lipidType(type)
                .numChains(chains).numCarbons(carbons).doubleBonds(doubleBonds).build();
    }

    private static Compound classifiedLipid(String code) {
        var compound = lipid(1, "PC", 2, 36, 2);
        compound.setLipidMapsClassifications(Set.of(new LipidMapsClassification(code.substring(0, 2),
                code.substring(0, 4), code.length() >= 6 ? code.substring(0, 6) : null, null)));
        return compound;
    }

    private static AnnotatedFeature feature(double mz, double rt, String adduct, Compound compound) {
        var feature = new AnnotatedFeature(new LCMSFeature(rt, mz));
        feature.addCompoundForAdduct(adduct, compound);
        return feature;
    }

    private static CompoundScores scores(AnnotatedFeature feature) {
        return (CompoundScores) feature.getAnnotationsByAdducts().getFirst().getAnnotations().getFirst().getScores().getFirst();
    }

    private static ExperimentParameters parameters(IonizationMode mode, ModifierType modifier) {
        var parameters = new ExperimentParameters();
        parameters.setIonMode(Optional.of(mode));
        parameters.setModifierType(Optional.of(modifier));
        return parameters;
    }
}
