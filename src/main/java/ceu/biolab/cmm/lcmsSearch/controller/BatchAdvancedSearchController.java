package ceu.biolab.cmm.lcmsSearch.controller;


import ceu.biolab.cmm.lcmsSearch.dto.BatchAdvancedSearchRequestDTO;
import ceu.biolab.cmm.lcmsSearch.service.BatchAdvancedSearchService;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "https://localhost:3000"}) // Allow frontend requests
public class BatchAdvancedSearchController {
    private final BatchAdvancedSearchService batchAdvancedSearchService;

    public BatchAdvancedSearchController(BatchAdvancedSearchService batchAdvancedSearchService) {
        this.batchAdvancedSearchService = batchAdvancedSearchService;
    }

    @PostMapping("/batch-advanced-search")
    public ResponseEntity<List<AnnotatedFeature>> scoreAnnotatedMSFeature(@Valid @RequestBody BatchAdvancedSearchRequestDTO request) {
        List<AnnotatedFeature> annotatedScoredFeatures = batchAdvancedSearchService.annotateAndScoreCmpoundsByMz(request);
        return ResponseEntity.ok(annotatedScoredFeatures);
    }

}
