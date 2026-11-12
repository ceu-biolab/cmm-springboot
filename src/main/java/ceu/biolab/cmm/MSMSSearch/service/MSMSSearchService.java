package ceu.biolab.cmm.MSMSSearch.service;

import ceu.biolab.cmm.MSMSSearch.domain.MSMSAnnotation;
import ceu.biolab.cmm.MSMSSearch.domain.Spectrum;
import ceu.biolab.cmm.MSMSSearch.dto.LCMSMSFeatureResponseDTO;
import ceu.biolab.cmm.MSMSSearch.dto.LCMSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMSSearch.dto.LCMSMSSearchResponseDTO;
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
import java.util.Collections;
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

    public LCMSMSSearchResponseDTO searchWithLcmsScoring(LCMSMSSearchRequestDTO request) {
        List<LCMSMSFeatureInput> featureInputs = normalizeLcmsmsFeatures(request);
        validateLcmsmsRequest(request, featureInputs);

        LCMSMSSearchResponseDTO response = new LCMSMSSearchResponseDTO();
        for (LCMSMSFeatureInput featureInput : featureInputs) {
            MSMSSearchResponseDTO searchResponse = executeSearch(toMsmsSearchRequest(request, featureInput));
            enrichWithLcmsScores(searchResponse, featureInput.rtValue(), featureInput.precursorIonMz(), request);
            response.addMsmsFeature(toFeatureResponse(searchResponse, featureInput));
        }

        if (response.getMsmsFeatures().size() == 1) {
            LCMSMSFeatureResponseDTO feature = response.getMsmsFeatures().getFirst();
            response.setMsmsList(feature.getMsmsList());
            response.setExperimentalSpectrum(feature.getExperimentalSpectrum());
        }
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

    private void enrichWithLcmsScores(MSMSSearchResponseDTO response, double rtValue, double precursorIonMz, LCMSMSSearchRequestDTO request) {
        if (response == null || response.getMsmsList() == null || response.getMsmsList().isEmpty()) {
            return;
        }

        AnnotatedFeature scoredFeature = new AnnotatedFeature(new LCMSFeature(rtValue, precursorIonMz));
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

    private void validateLcmsmsRequest(LCMSMSSearchRequestDTO request, List<LCMSMSFeatureInput> featureInputs) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required.");
        }

        validateCommonRequest(request);

        if (featureInputs.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one feature with RT and spectrum is required for LC scoring.");
        }

        for (LCMSMSFeatureInput featureInput : featureInputs) {
            if (featureInput.precursorIonMz() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Precursor m/z is required for each feature.");
            }
            if (featureInput.rtValue() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Retention-time values are required for LC scoring.");
            }
            validateSpectrum(featureInput.spectrum());
        }
    }

    private void validateCommonRequest(LCMSMSSearchRequestDTO request) {
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
    }

    private void validateSpectrum(Spectrum spectrum) {
        if (spectrum == null || spectrum.getPeaks() == null || spectrum.getPeaks().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fragment peaks are required.");
        }
    }

    private List<LCMSMSFeatureInput> normalizeLcmsmsFeatures(LCMSMSSearchRequestDTO request) {
        boolean hasSingleFeature = request != null && (
                request.getPrecursorIonMZ() != null
                        || request.getFragmentsMZsIntensities() != null
                        || request.getRtValue() != null
        );
        boolean hasBatchFeatures = request != null && (
                hasValues(request.getPrecursorIonMZValues())
                        || hasValues(request.getFragmentsMZsIntensitiesList())
                        || hasValues(request.getRtValues())
        );

        if (hasSingleFeature && hasBatchFeatures) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Use either single-feature fields or batched feature lists, not both.");
        }

        if (hasBatchFeatures) {
            int size = sized(request.getRtValues(), "rtValues");
            requireSameSize(size, request.getPrecursorIonMZValues(), "precursorIonMZValues");
            requireSameSize(size, request.getFragmentsMZsIntensitiesList(), "fragmentsMZsIntensitiesList");

            List<LCMSMSFeatureInput> featureInputs = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                featureInputs.add(new LCMSMSFeatureInput(
                        request.getPrecursorIonMZValues().get(i),
                        request.getFragmentsMZsIntensitiesList().get(i),
                        request.getRtValues().get(i)
                ));
            }
            return featureInputs;
        }

        if (!hasSingleFeature) {
            return Collections.emptyList();
        }

        return List.of(new LCMSMSFeatureInput(
                request.getPrecursorIonMZ(),
                request.getFragmentsMZsIntensities(),
                request.getRtValue()
        ));
    }

    private int sized(List<?> values, String fieldName) {
        if (!hasValues(values)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required.");
        }
        return values.size();
    }

    private void requireSameSize(int expectedSize, List<?> values, String fieldName) {
        if (!hasValues(values)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required.");
        }
        if (values.size() != expectedSize) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Number of " + fieldName + " entries must match rtValues.");
        }
    }

    private boolean hasValues(List<?> values) {
        return values != null && !values.isEmpty();
    }

    private MSMSSearchRequestDTO toMsmsSearchRequest(LCMSMSSearchRequestDTO request, LCMSMSFeatureInput featureInput) {
        MSMSSearchRequestDTO msmsRequest = new MSMSSearchRequestDTO();
        msmsRequest.setCIDEnergy(request.getCIDEnergy());
        msmsRequest.setPrecursorIonMZ(featureInput.precursorIonMz());
        msmsRequest.setTolerancePrecursorIon(request.getTolerancePrecursorIon());
        msmsRequest.setToleranceModePrecursorIon(request.getToleranceModePrecursorIon());
        msmsRequest.setToleranceFragments(request.getToleranceFragments());
        msmsRequest.setToleranceModeFragments(request.getToleranceModeFragments());
        msmsRequest.setIonizationMode(request.getIonizationMode());
        msmsRequest.setAdducts(request.getAdducts());
        msmsRequest.setFragmentsMZsIntensities(featureInput.spectrum());
        msmsRequest.setScoreType(request.getScoreType());
        msmsRequest.setSpectrumSource(request.getSpectrumSource());
        return msmsRequest;
    }

    private LCMSMSFeatureResponseDTO toFeatureResponse(MSMSSearchResponseDTO response, LCMSMSFeatureInput featureInput) {
        LCMSMSFeatureResponseDTO featureResponse = new LCMSMSFeatureResponseDTO();
        featureResponse.setFeature(new LCMSFeature(featureInput.rtValue(), featureInput.precursorIonMz()));
        featureResponse.setMsmsList(response.getMsmsList());
        featureResponse.setExperimentalSpectrum(response.getExperimentalSpectrum());
        return featureResponse;
    }

    private record LCMSMSFeatureInput(double precursorIonMz, Spectrum spectrum, Double rtValue) {
    }
}
