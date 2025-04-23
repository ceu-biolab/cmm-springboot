package ceu.biolab.cmm.rtSearch.api;
import ceu.biolab.cmm.rtSearch.dto.CompoundBatchSearchRequestDTO;
import ceu.biolab.cmm.rtSearch.dto.CompoundSimpleSearchRequestDTO;
import ceu.biolab.cmm.rtSearch.service.CompoundService;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/compounds")
@CrossOrigin(origins = {"http://localhost:3000", "https://localhost:3000"}) // Allow frontend requests

public class CompoundController {
    private final CompoundService compoundService;
    private static final Logger logger = LoggerFactory.getLogger(CompoundController.class);

    public CompoundController(CompoundService compoundService) {
        this.compoundService = compoundService;
    }

    @PostMapping("/simple-search")
    public List<AnnotatedFeature> annotateMSFeature(@RequestBody CompoundSimpleSearchRequestDTO request) {
        List<AnnotatedFeature> features = compoundService.findCompoundsByMz(
                request.getMz(),
                request.getMzToleranceMode(),
                request.getTolerance(),
                request.getIonizationMode(),
                request.getAdductsString(),
                request.getDatabases(),
                request.getMetaboliteType()
        );
        return features;
    }

    @PostMapping("/batch-search")
    public List<AnnotatedFeature> annotateMSFeatures(@RequestBody CompoundBatchSearchRequestDTO request) {
        List<AnnotatedFeature> features = new ArrayList<>();
        for(Double mz : request.getMzValues()) {
            List<AnnotatedFeature> result = compoundService.findCompoundsByMz(
                    mz,
                    request.getMzToleranceMode(),
                    request.getTolerance(),
                    request.getIonizationMode(),
                    request.getAdductsString(),
                    request.getDatabases(),
                    request.getMetaboliteType()
            );

            features.addAll(result);
        }

        return features;
    }

    @GetMapping("/simple-search")
    public List<AnnotatedFeature> annotatedMSFeatures(@RequestBody CompoundSimpleSearchRequestDTO request) {
        List<AnnotatedFeature> features = compoundService.findCompoundsByMz(
                request.getMz(),
                request.getMzToleranceMode(),
                request.getTolerance(),
                request.getIonizationMode(),
                request.getAdductsString(),
                request.getDatabases(),
                request.getMetaboliteType()
        );
        return features;
    }

    @GetMapping("/batch-search")
    public List<AnnotatedFeature> annotatedMSFeatures(@RequestBody CompoundBatchSearchRequestDTO request) {
        List<AnnotatedFeature> features = new ArrayList<>();
        for(Double mz : request.getMzValues()) {
            List<AnnotatedFeature> result = compoundService.findCompoundsByMz(
                    mz,
                    request.getMzToleranceMode(),
                    request.getTolerance(),
                    request.getIonizationMode(),
                    request.getAdductsString(),
                    request.getDatabases(),
                    request.getMetaboliteType()
            );
            features.addAll(result);
        }
        return features;
    }
}

