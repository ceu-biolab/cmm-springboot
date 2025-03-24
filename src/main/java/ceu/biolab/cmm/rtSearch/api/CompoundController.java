package ceu.biolab.cmm.rtSearch.api;
import ceu.biolab.cmm.rtSearch.model.msFeature.MSFeature;
import ceu.biolab.cmm.rtSearch.service.CompoundService;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/compounds")
@CrossOrigin(origins = {"http://localhost:3000", "https://localhost:3000"}) // Allow frontend requests

public class CompoundController {
    private final CompoundService compoundService;

    public CompoundController(CompoundService compoundService) {
        this.compoundService = compoundService;
    }

    @PostMapping("/search")
    public Set<MSFeature> annotateMSFeatures(@RequestBody CompoundSearchRequest request) {
        Set<MSFeature> features = compoundService.findCompoundsByMzRanges(
                request.getMz(),
                request.getToleranceMode(),
                request.getTolerance(),
                request.getIonizationMode(),
                request.getAdductsString(),
                request.getDatabases(),
                request.getMetaboliteType()
        );

        System.out.println("Found features: " + features);
        return features;
    }

    @GetMapping("/search")
    public Set<MSFeature> annotatedMSFeatures(@RequestBody CompoundSearchRequest request) {

        Set<MSFeature> features = compoundService.findCompoundsByMzRanges(
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

}
