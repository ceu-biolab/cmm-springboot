package ceu.biolab.cmm.msSearch.controller;
import ceu.biolab.cmm.msSearch.dto.CompoundBatchSearchRequestDTO;
import ceu.biolab.cmm.msSearch.dto.CompoundSimpleSearchRequestDTO;
import ceu.biolab.cmm.msSearch.dto.RTSearchResponseDTO;
import ceu.biolab.cmm.msSearch.service.CompoundService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

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
    public RTSearchResponseDTO annotateMSFeature(@Valid @RequestBody CompoundSimpleSearchRequestDTO request) {
        return compoundService.findCompoundsByMz(request);
    }

    @PostMapping("/batch-search")
    public RTSearchResponseDTO annotateMSFeatures(@Valid @RequestBody CompoundBatchSearchRequestDTO request) {
        if (request.getMzValues().stream().anyMatch(Objects::isNull)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mzValues must not contain null entries.");
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
