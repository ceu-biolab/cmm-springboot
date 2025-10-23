package ceu.biolab.cmm.scoreAnnotations.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import ceu.biolab.cmm.scoreAnnotations.dto.ScoreAnnotationsRequest;
import ceu.biolab.cmm.scoreAnnotations.service.ScoreAnnotationsService;
import ceu.biolab.cmm.shared.domain.ExperimentParameters;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;

@RestController
@RequestMapping("/api")
public class ScoreAnnotationsController {
    
    @PostMapping("/score-annotations")
    public ResponseEntity<List<AnnotatedFeature>> scoreAnnotations(@Valid @RequestBody ScoreAnnotationsRequest request) {
        List<AnnotatedFeature> features = request.getFeatures();
        Optional<ExperimentParameters> experimentParameters = Optional.ofNullable(request.getExperimentParameters());
        
        // Apply scoring to the features
        ScoreAnnotationsService.scoreAnnotations(features, experimentParameters);
        
        // Return the same features, now with scores
        return ResponseEntity.ok(features);
    }
}
