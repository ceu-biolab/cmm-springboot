package ceu.biolab.cmm.scoreAnnotations.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import ceu.biolab.cmm.scoreAnnotations.domain.CompoundScores;
import ceu.biolab.cmm.scoreAnnotations.domain.EvaluatedCompound;
import ceu.biolab.cmm.scoreAnnotations.domain.EvaluatedLipid;
import ceu.biolab.cmm.scoreAnnotations.domain.Lipid;
import ceu.biolab.cmm.scoreAnnotations.domain.LipidScores;
import ceu.biolab.cmm.shared.domain.ExperimentParameters;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.compound.CompoundType;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import ceu.biolab.cmm.shared.domain.msFeature.Annotation;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotationsByAdduct;
import ceu.biolab.cmm.shared.domain.msFeature.ILCMSFeature;

public class ScoreAnnotationsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreAnnotationsService.class);

    private ScoreAnnotationsService() {
        // utility
    }

    public static void scoreAnnotations(List<AnnotatedFeature> msFeatures, Optional<ExperimentParameters> experimentParameters) {
        KieSession kieSession = null;
        try {
            if (msFeatures == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Feature list is required for scoring.");
            }
            if (msFeatures.stream().anyMatch(Objects::isNull)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Feature list must not contain null entries.");
            }

            KieServices kieServices = KieServices.Factory.get();
            KieContainer kieContainer = kieServices.newKieClasspathContainer();

            try {
                kieSession = kieContainer.newKieSession("lipidKSession");
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create scoring session", e);
            }

            if (kieSession == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create scoring session");
            }

            for (AnnotatedFeature msFeature : msFeatures) {
                if (!(msFeature.getFeature() instanceof ILCMSFeature lcFeature)) {
                    continue;
                }

                double featureMz = lcFeature.getMzValue();
                double featureRtValue = lcFeature.getRtValue();

                for (AnnotationsByAdduct annotationsByAdduct : msFeature.getAnnotationsByAdducts()) {
                    String adduct = annotationsByAdduct.getAdduct();
                    for (Annotation annotation : annotationsByAdduct.getAnnotations()) {
                        Compound compound = annotation.getCompound();
                        if (compound == null) {
                            LOGGER.warn("Skipping annotation without compound for feature mz {} / rt {}", featureMz, featureRtValue);
                            continue;
                        }
                        CompoundType compoundType = compound.getCompoundType();

                        if (CompoundType.LIPID.equals(compoundType) || compound instanceof Lipid) {
                            Lipid lipid;
                            if (compound instanceof Lipid lipidCompound) {
                                lipid = lipidCompound;
                            } else {
                                try {
                                    lipid = new Lipid(compound);
                                } catch (IllegalArgumentException e) {
                                    LOGGER.warn("Invalid lipid compound {}: {}", compound.getCompoundId(), e.getMessage());
                                    continue;
                                }
                            }
                            LipidScores scores = new LipidScores();
                            annotation.addScore(scores);
                            EvaluatedLipid evaluatedLipid = new EvaluatedLipid(lipid, featureMz, featureRtValue, adduct, scores);
                            kieSession.insert(evaluatedLipid);
                        } else {
                            CompoundScores scores = new CompoundScores();
                            annotation.addScore(scores);
                            EvaluatedCompound evaluatedCompound = new EvaluatedCompound(compound, featureMz, featureRtValue, adduct, scores);
                            kieSession.insert(evaluatedCompound);
                        }
                    }
                }
            }

            kieSession.insert(experimentParameters.orElseGet(ExperimentParameters::empty));

            kieSession.fireAllRules();
        } catch (ResponseStatusException ex) {
            LOGGER.error("Scoring failed: {}", ex.getReason(), ex);
            throw ex;
        } catch (Exception e) {
            LOGGER.error("Unexpected error while scoring annotations", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to score annotations", e);
        } finally {
            if (kieSession != null) {
                kieSession.dispose();
            }
        }
    }
}
