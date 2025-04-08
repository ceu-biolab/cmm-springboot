package ceu.biolab.cmm.rtSearch.api;
import ceu.biolab.cmm.rtSearch.model.msFeature.MSFeature;
import ceu.biolab.cmm.rtSearch.service.CompoundService;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/compounds")
@CrossOrigin(origins = {"http://localhost:3000", "https://localhost:3000"}) // Allow frontend requests

public class CompoundController {
    private final CompoundService compoundService;

    public CompoundController(CompoundService compoundService) {
        this.compoundService = compoundService;
    }

    @PostMapping("/simple-search")
    public Set<MSFeature> annotateMSFeature(@RequestBody CompoundSimpleSearchRequest request) {
        Set<MSFeature> features = compoundService.findCompoundsByMz(
                request.getMz(),
                request.getToleranceMode(),
                request.getTolerance(),
                request.getIonizationMode(),
                request.getAdductsString(),
                request.getDatabases(),
                request.getMetaboliteType()
        );

        return features;
    }

    @PostMapping("/batch-search")
    public Set<MSFeature> annotateMSFeatures(@RequestBody CompoundBatchSearchRequest request) {
        Set<MSFeature> features = new HashSet<>();
        for(Double mz : request.getMz()) {
            features = compoundService.findCompoundsByMz(
                    mz,
                    request.getToleranceMode(),
                    request.getTolerance(),
                    request.getIonizationMode(),
                    request.getAdductsString(),
                    request.getDatabases(),
                    request.getMetaboliteType()
            );
        }

        return features;
    }

    @GetMapping("/simple-search")
    public Set<MSFeature> annotatedMSFeatures(@RequestBody CompoundSimpleSearchRequest request) {

        Set<MSFeature> features = compoundService.findCompoundsByMz(
                request.getMz(),
                request.getToleranceMode(),
                request.getTolerance(),
                request.getIonizationMode(),
                request.getAdductsString(),
                request.getDatabases(),
                request.getMetaboliteType()
        );
        return features;
    }

    @GetMapping("/batch-search")
    public Set<MSFeature> annotatedMSFeatures(@RequestBody CompoundBatchSearchRequest request) {

        Set<MSFeature> features = new HashSet<>();
        for(Double mz : request.getMz()) {
            features = compoundService.findCompoundsByMz(
                    mz,
                    request.getToleranceMode(),
                    request.getTolerance(),
                    request.getIonizationMode(),
                    request.getAdductsString(),
                    request.getDatabases(),
                    request.getMetaboliteType()
            );
        }
        return features;
    }
}