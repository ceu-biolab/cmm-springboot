package ceu.biolab.cmm.scoreAnnotations.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ceu.biolab.cmm.scoreAnnotations.dto.ScoreLipidRequest;
import ceu.biolab.cmm.scoreAnnotations.service.ScoreLipids;
import ceu.biolab.cmm.shared.domain.ExperimentParameters;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;

@RestController
@RequestMapping("/api/score")
public class ScoreAnnotationsController {
    
    @PostMapping("/lipids")
    public ResponseEntity<List<AnnotatedFeature>> scoreLipidAnnotations(@RequestBody ScoreLipidRequest request) {
        List<AnnotatedFeature> features = request.getFeatures();
        Optional<ExperimentParameters> experimentParameters = Optional.ofNullable(request.getExperimentParameters());
        
        // Apply scoring to the features
        ScoreLipids.scoreLipidAnnotations(features, experimentParameters);
        
        // Return the same features, now with scores
        return ResponseEntity.ok(features);
    }
}
