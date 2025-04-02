package ceu.biolab.cmm.scoreAnnotations.service;

import java.util.ArrayList;
import java.util.List;

import org.drools.ruleunits.api.RuleUnitProvider;
import org.drools.ruleunits.api.RuleUnitInstance;

import ceu.biolab.cmm.scoreAnnotations.model.*;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import ceu.biolab.cmm.shared.domain.msFeature.Annotation;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotationsByAdduct;
import ceu.biolab.cmm.shared.domain.msFeature.ILCFeature;
import ceu.biolab.cmm.shared.domain.msFeature.IMSFeature;
import ceu.biolab.cmm.shared.domain.msFeature.LCMSFeature;

public class ScoreLipids {

    public static void scoreLipidAnnotations(List<AnnotatedFeature> msFeatures) {
        LipidsUnit compoundsUnit = new LipidsUnit();
        RuleUnitInstance<LipidsUnit> ruleUnitInstance = RuleUnitProvider.get().createRuleUnitInstance(compoundsUnit);

        for (AnnotatedFeature msFeature : msFeatures) {
            double featureMz, featureRtValue;
            // Feature has to have rt value for scoring
            if (msFeature.getFeature() instanceof ILCFeature lcFeature) {
                featureMz = lcFeature.getMzValue();
                featureRtValue = lcFeature.getRetentionTime();
            } else {
                continue;
            }

            // Map all lipids into EvaluatedLipid objects for scoring
            for (AnnotationsByAdduct annotationsByAdduct : msFeature.getAnnotationsByAdducts()) {
                for (Annotation annotation : annotationsByAdduct.getAnnotations()) {
                    if (annotation.getCompound() instanceof Lipid lipid) {
                        LipidScores scores = new LipidScores();
                        annotation.addScore(scores);
                        // A bit yank, but scores will be updated through shared reference in the returned object
                        EvaluatedLipid evaluatedLipid = new EvaluatedLipid(lipid, featureMz, featureRtValue, scores);
                        compoundsUnit.getCompounds().add(evaluatedLipid);
                    }
                }
            }
        }

        ruleUnitInstance.fire();
    }

}
