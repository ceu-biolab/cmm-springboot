package ceu.biolab.cmm.MSMSSearch.service;

import ceu.biolab.cmm.MSMSSearch.domain.MSMSAnnotation;
import ceu.biolab.cmm.MSMSSearch.dto.LCMSMSSearchRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchResponseDTO;
import ceu.biolab.cmm.MSMSSearch.repository.MSMSSearchRepository;
import ceu.biolab.cmm.scoreAnnotations.service.ScoreAnnotationsService;
import ceu.biolab.cmm.shared.domain.ExperimentParameters;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import ceu.biolab.cmm.shared.domain.msFeature.Annotation;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotationsByAdduct;
import ceu.biolab.cmm.shared.domain.msFeature.LCMSFeature;
import ceu.biolab.cmm.shared.validation.MzToleranceLimits;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MSMSSearchService {
    private final MSMSSearchRepository msmsSearchRepository;

    @Autowired
    public MSMSSearchService(MSMSSearchRepository msmsSearchRepository) {
        this.msmsSearchRepository = msmsSearchRepository;
    }

    public MSMSSearchResponseDTO search(MSMSSearchRequestDTO request) {
        validateRequest(request);
        return executeSearch(request);
    }

    public MSMSSearchResponseDTO searchWithLcmsScoring(LCMSMSSearchRequestDTO request) {
        validateRequest(request);
        if (request.getRtValue() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Retention-time value is required for LC scoring.");
        }

        MSMSSearchResponseDTO response = executeSearch(request);
        enrichWithLcmsScores(response, request);
        return response;
    }

    private void validateRequest(MSMSSearchRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required.");
        }

        if (request.getPrecursorIonMZ() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Precursor m/z is required.");
        }

        if (request.getAdducts() == null || request.getAdducts().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must provide at least one adduct.");
        }

        if (request.getIonizationMode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ionization mode is required.");
        }
        if (request.getIonizationMode() == IonizationMode.NEUTRAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Neutral ionization mode is not supported.");
        }

        if (request.getCIDEnergy() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CID energy is required.");
        }
        if (request.getScoreType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Score type is required.");
        }
        if (request.getSpectrumSource() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Spectrum source is required.");
        }
        if (request.getTolerancePrecursorIon() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Precursor tolerance must be greater than zero.");
        }
        if (request.getToleranceFragments() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fragment tolerance must be greater than zero.");
        }
        if (request.getToleranceModePrecursorIon() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Precursor tolerance mode is required.");
        }
        if (request.getToleranceModeFragments() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fragment tolerance mode is required.");
        }
        if (MzToleranceLimits.exceedsLimit(request.getTolerancePrecursorIon(), request.getToleranceModePrecursorIon())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MzToleranceLimits.violationMessage("tolerancePrecursorIon", request.getToleranceModePrecursorIon()));
        }
        if (MzToleranceLimits.exceedsLimit(request.getToleranceFragments(), request.getToleranceModeFragments())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    MzToleranceLimits.violationMessage("toleranceFragments", request.getToleranceModeFragments()));
        }

        if (request.getFragmentsMZsIntensities() == null
                || request.getFragmentsMZsIntensities().getPeaks() == null
                || request.getFragmentsMZsIntensities().getPeaks().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fragment peaks are required.");
        }
    }

    private MSMSSearchResponseDTO executeSearch(MSMSSearchRequestDTO request) {
        try {
            return msmsSearchRepository.findMatchingCompoundsAndSpectra(request);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to execute MS/MS search",
                    e
            );
        }
    }

    private void enrichWithLcmsScores(MSMSSearchResponseDTO response, LCMSMSSearchRequestDTO request) {
        if (response == null || response.getMsmsList() == null || response.getMsmsList().isEmpty()) {
            return;
        }

        AnnotatedFeature scoredFeature = new AnnotatedFeature(new LCMSFeature(request.getRtValue(), request.getPrecursorIonMZ()));
        Map<String, MSMSAnnotation> hitByCompoundAndAdduct = new LinkedHashMap<>();
        Map<String, AnnotationsByAdduct> annotationsByAdduct = new LinkedHashMap<>();

        for (MSMSAnnotation hit : response.getMsmsList()) {
            if (hit.getCompound() == null) {
                continue;
            }

            String adduct = hit.getAdduct() == null ? "" : hit.getAdduct();
            String key = annotationKey(hit.getCompound().getCompoundId(), adduct);
            hitByCompoundAndAdduct.put(key, hit);

            Annotation annotation = new Annotation(hit.getCompound());
            annotationsByAdduct
                    .computeIfAbsent(adduct, AnnotationsByAdduct::new)
                    .addAnnotation(annotation);
        }

        if (annotationsByAdduct.isEmpty()) {
            return;
        }

        scoredFeature.setAnnotationsByAdducts(new ArrayList<>(annotationsByAdduct.values()));
        ScoreAnnotationsService.scoreAnnotations(
                List.of(scoredFeature),
                Optional.of(resolveExperimentParameters(request))
        );

        for (AnnotationsByAdduct annotationsForAdduct : scoredFeature.getAnnotationsByAdducts()) {
            String adduct = annotationsForAdduct.getAdduct() == null ? "" : annotationsForAdduct.getAdduct();
            for (Annotation scoredAnnotation : annotationsForAdduct.getAnnotations()) {
                if (scoredAnnotation.getCompound() == null) {
                    continue;
                }
                MSMSAnnotation originalHit = hitByCompoundAndAdduct.get(
                        annotationKey(scoredAnnotation.getCompound().getCompoundId(), adduct)
                );
                if (originalHit != null) {
                    originalHit.setScores(scoredAnnotation.getScores());
                }
            }
        }
    }

    private ExperimentParameters resolveExperimentParameters(LCMSMSSearchRequestDTO request) {
        ExperimentParameters experimentParameters = Optional.ofNullable(request.getExperimentParameters())
                .orElseGet(ExperimentParameters::new);
        if (experimentParameters.getIonMode() == null || experimentParameters.getIonMode().isEmpty()) {
            experimentParameters.setIonMode(Optional.ofNullable(request.getIonizationMode()));
        }
        return experimentParameters;
    }

    private String annotationKey(int compoundId, String adduct) {
        return compoundId + "|" + adduct;
    }
}
