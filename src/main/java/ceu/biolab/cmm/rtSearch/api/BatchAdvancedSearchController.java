package ceu.biolab.cmm.rtSearch.api;


import ceu.biolab.cmm.rtSearch.dto.BatchAdvancedSearchRequestDTO;
import ceu.biolab.cmm.rtSearch.service.BatchAdvancedSearchService;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
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
    public ResponseEntity<List<AnnotatedFeature>> scoreAnnotatedMSFeature(@RequestBody BatchAdvancedSearchRequestDTO request) {
        List<AnnotatedFeature> annotatedScoredFeatures = batchAdvancedSearchService.annotateAndScoreCmpoundsByMz(request);
        return ResponseEntity.ok(annotatedScoredFeatures);
    }

}
