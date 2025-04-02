package ceu.biolab.cmm.scoreAnnotations.service;

import java.util.ArrayList;
import java.util.List;

import org.drools.ruleunits.api.RuleUnitProvider;
import org.drools.ruleunits.api.RuleUnitInstance;

import ceu.biolab.cmm.scoreAnnotations.dto.*;
import ceu.biolab.cmm.scoreAnnotations.model.*;
import ceu.biolab.cmm.shared.domain.AnnotatedRTFeature;

public class ScoreLipids {

    public static List<ScoredAnnotatedRTFeature> scoreLipids(List<AnnotatedRTFeature> msFeatures) {
        LipidsUnit compoundsUnit = new LipidsUnit();
        RuleUnitInstance<LipidsUnit> ruleUnitInstance = RuleUnitProvider.get().createRuleUnitInstance(compoundsUnit);

        List<ScoredAnnotatedRTFeature> scoredFeatures = new ArrayList<>();

        for (AnnotatedRTFeature msFeature : msFeatures) {
            ScoredAnnotatedRTFeature scoredAnnotatedRTFeature = new ScoredAnnotatedRTFeature(msFeature);
            scoredFeatures.add(scoredAnnotatedRTFeature);
            double featureRtValue = scoredAnnotatedRTFeature.getRtValue();
            double featureMz = scoredAnnotatedRTFeature.getMzValue();

            // Map all lipids into EvaluatedLipid objects for scoring
            for (ScoredAnnotationsByAdduct scoredAnnotationsByAdduct : scoredAnnotatedRTFeature.getScoredAnnotationsByAdducts()) {
                for (ScoredCompound scoredCompound : scoredAnnotationsByAdduct.getAnnotations()) {
                    if (scoredCompound.getCompound() instanceof Lipid lipid) {
                        LipidScores scores = new LipidScores();
                        scoredCompound.setScores(scores);
                        // A bit yank, but scores will be updated through shared reference in the returned object
                        EvaluatedLipid evaluatedLipid = new EvaluatedLipid(lipid, featureMz, featureRtValue, scores);
                        compoundsUnit.getCompounds().add(evaluatedLipid);
                    }
                }
            }
        }

        ruleUnitInstance.fire();

        return scoredFeatures;
    }

}
