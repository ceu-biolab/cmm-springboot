package ceu.biolab.cmm.scoreAnnotations.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ceu.biolab.cmm.scoreAnnotations.domain.EvaluatedLipid;
import ceu.biolab.cmm.scoreAnnotations.domain.Lipid;
import ceu.biolab.cmm.scoreAnnotations.domain.LipidScores;
import ceu.biolab.cmm.shared.domain.ExperimentParameters;
import ceu.biolab.cmm.shared.domain.compound.CompoundType;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import ceu.biolab.cmm.shared.domain.msFeature.Annotation;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotationsByAdduct;
import ceu.biolab.cmm.shared.domain.msFeature.ILCMSFeature;

public class ScoreLipids {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreLipids.class);
    public static void scoreLipidAnnotations(List<AnnotatedFeature> msFeatures, Optional<ExperimentParameters> experimentParameters){//}, Optional<Double> retentionTimes) {
        KieSession kieSession = null;
        try {
            if (msFeatures == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Feature list is required for lipid scoring.");
            }
            if (msFeatures.stream().anyMatch(Objects::isNull)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Feature list must not contain null entries.");
            }
            // Create a KieSession - using the simpler approach
            KieServices kieServices = KieServices.Factory.get();
            KieContainer kieContainer = kieServices.newKieClasspathContainer();

            // Load the session by name from kmodule.xml
            try {
                kieSession = kieContainer.newKieSession("lipidKSession");
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create lipid scoring session", e);
            }

            if (kieSession == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create lipid scoring session");
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
                        CompoundType compoundType = annotation.getCompound().getCompoundType();
                        if (CompoundType.LIPID.equals(compoundType) || annotation.getCompound() instanceof Lipid) {
                            Lipid lipid;
                            if (!(annotation.getCompound() instanceof Lipid)) {
                                // Convert compounds that are not sent as Lipid objects
                                try {
                                    lipid = new Lipid(annotation.getCompound());
                                } catch (IllegalArgumentException e) {
                                    // Handle the case where the compound is not a valid lipid
                                    LOGGER.warn("Invalid lipid compound: {}", annotation.getCompound().getCompoundId(), e.getMessage());
                                    continue;
                                }
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
        } catch (ResponseStatusException ex) {
            LOGGER.error("Lipid scoring failed: {}", ex.getReason(), ex);
            throw ex;
        } catch (Exception e) {
            LOGGER.error("Unexpected error while scoring lipid annotations", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to score lipid annotations", e);
        } finally {
            // Ensure the session is properly disposed, only if it was created
            if (kieSession != null) {
                kieSession.dispose();
            }
        }
    }
}
