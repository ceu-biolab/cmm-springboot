package ceu.biolab.cmm.msSearch.controller;
import ceu.biolab.cmm.msSearch.dto.CompoundBatchSearchRequestDTO;
import ceu.biolab.cmm.msSearch.dto.CompoundSimpleSearchRequestDTO;
import ceu.biolab.cmm.msSearch.dto.RTSearchResponseDTO;
import ceu.biolab.cmm.msSearch.service.CompoundService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compounds")
@CrossOrigin(origins = {"http://localhost:3000", "https://localhost:3000"}) // Allow frontend requests

public class CompoundController {
    private final CompoundService compoundService;
    // private static final Logger logger = LoggerFactory.getLogger(CompoundController.class);

    public CompoundController(CompoundService compoundService) {
        this.compoundService = compoundService;
    }

    @PostMapping("/simple-search")
    public RTSearchResponseDTO annotateMSFeature(@RequestBody CompoundSimpleSearchRequestDTO request) {
        if (request.getMz() == null) {
            return new RTSearchResponseDTO();
        }
        return compoundService.findCompoundsByMz(request);
    }

    @PostMapping("/batch-search")
    public RTSearchResponseDTO annotateMSFeatures(@RequestBody CompoundBatchSearchRequestDTO request) {
        if (request.getMzValues() == null || request.getMzValues().isEmpty()) {
            return new RTSearchResponseDTO();
        }

        RTSearchResponseDTO response = new RTSearchResponseDTO();

        for (Double mz : request.getMzValues()) {
            CompoundSimpleSearchRequestDTO simpleRequest = new CompoundSimpleSearchRequestDTO(
                    mz,
                    request.getMzToleranceMode(),
                    request.getTolerance(),
                    request.getIonizationMode(),
                    request.getAdductsString(),
                    request.getDetectedAdduct(),
                    request.getFormulaType(),
                    request.getDatabases(),
                    request.getMetaboliteType()
            );

            RTSearchResponseDTO result = compoundService.findCompoundsByMz(simpleRequest);
            response.getMSFeatures().addAll(result.getMSFeatures());
        }

        return response;
    }

}

