package ceu.biolab.cmm.scoreAnnotations.service;

import java.util.List;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import ceu.biolab.cmm.scoreAnnotations.model.*;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import ceu.biolab.cmm.shared.domain.msFeature.Annotation;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotationsByAdduct;
import ceu.biolab.cmm.shared.domain.msFeature.ILCFeature;

public class ScoreLipids {

    public static void scoreLipidAnnotations(List<AnnotatedFeature> msFeatures) {
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
                if (msFeature.getFeature() instanceof ILCFeature lcFeature) {
                    featureMz = lcFeature.getMzValue();
                    featureRtValue = lcFeature.getRetentionTime();
                } else {
                    continue;
                }

                // Map all lipids into EvaluatedLipid objects for scoring
                for (AnnotationsByAdduct annotationsByAdduct : msFeature.getAnnotationsByAdducts()) {
                    String adduct = annotationsByAdduct.getAdduct();
                    for (Annotation annotation : annotationsByAdduct.getAnnotations()) {
                        if (annotation.getCompound() instanceof Lipid lipid) {
                            LipidScores scores = new LipidScores();
                            annotation.addScore(scores);
                            EvaluatedLipid evaluatedLipid = new EvaluatedLipid(lipid, featureMz, featureRtValue, adduct, scores);
                            kieSession.insert(evaluatedLipid);
                        }
                    }
                }
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
