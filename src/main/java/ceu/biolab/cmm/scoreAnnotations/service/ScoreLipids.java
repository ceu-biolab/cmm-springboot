package ceu.biolab.cmm.scoreAnnotations.service;

import java.util.List;
import java.util.Optional;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import ceu.biolab.cmm.scoreAnnotations.domain.EvaluatedLipid;
import ceu.biolab.cmm.scoreAnnotations.domain.Lipid;
import ceu.biolab.cmm.scoreAnnotations.domain.LipidScores;
import ceu.biolab.cmm.shared.domain.ExperimentParameters;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import ceu.biolab.cmm.shared.domain.msFeature.Annotation;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotationsByAdduct;
import ceu.biolab.cmm.shared.domain.msFeature.ILCMSFeature;

public class ScoreLipids {
    public static void scoreLipidAnnotations(List<AnnotatedFeature> msFeatures, Optional<ExperimentParameters> experimentParameters){//}, Optional<Double> retentionTimes) {
        KieSession kieSession = null;
        try {
            // Create a KieSession - using the simpler approach
            KieServices kieServices = KieServices.Factory.get();
            KieContainer kieContainer = kieServices.newKieClasspathContainer();
            
            // Load the session by name from kmodule.xml
            try {
                kieSession = kieContainer.newKieSession("lipidKSession");
            } catch (Exception e) {
                System.err.println("Error creating KieSession: " + e.getMessage());
                e.printStackTrace();
                return;
            }
            
            if (kieSession == null) {
                System.err.println("Failed to create KieSession: lipidKSession not found");
                return;
            }

            // Process the features and insert facts into the session
            for (AnnotatedFeature msFeature : msFeatures) {
                double featureMz, featureRtValue;
                // Feature has to have rt value for scoring
                if (msFeature.getFeature() instanceof ILCMSFeature lcFeature) {
                    featureMz = lcFeature.getMzValue();
                    featureRtValue = lcFeature.getRtValue();
                } else {
                    continue;
                }

                // Map all lipids into EvaluatedLipid objects for scoring
                for (AnnotationsByAdduct annotationsByAdduct : msFeature.getAnnotationsByAdducts()) {
                    String adduct = annotationsByAdduct.getAdduct();
                    for (Annotation annotation : annotationsByAdduct.getAnnotations()) {
                        //if (annotation.getCompound() instanceof Lipid lipid) {
                        if (annotation.getCompound().getCompoundType() == 1 || annotation.getCompound() instanceof Lipid) {
                            Lipid lipid;
                            if (!(annotation.getCompound() instanceof Lipid)) {
                                // Workaround for now in case lipid compounds are not send as Lipid objects
                                lipid = new Lipid(annotation.getCompound());
                            }
                            else {
                                lipid = (Lipid) annotation.getCompound();
                            }
                            LipidScores scores = new LipidScores();
                            annotation.addScore(scores);
                            EvaluatedLipid evaluatedLipid = new EvaluatedLipid(lipid, featureMz, featureRtValue, adduct, scores);
                            kieSession.insert(evaluatedLipid);
                        }
                    }
                }
            }

            if (experimentParameters.isPresent()) {
                kieSession.insert(experimentParameters.get());
            }
            else {
                // If experiment parameters are not present, insert a default one
                kieSession.insert(ExperimentParameters.empty());
            }

            // Fire all rules and let them operate on the inserted facts
            kieSession.fireAllRules();
        } finally {
            // Ensure the session is properly disposed, only if it was created
            if (kieSession != null) {
                kieSession.dispose();
            }
        }
    }
}
