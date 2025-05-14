package ceu.biolab.cmm.rtSearch.controller;
import ceu.biolab.cmm.rtSearch.dto.CompoundBatchSearchRequestDTO;
import ceu.biolab.cmm.rtSearch.dto.CompoundSimpleSearchRequestDTO;
import ceu.biolab.cmm.rtSearch.dto.RTSearchResponseDTO;
import ceu.biolab.cmm.rtSearch.service.CompoundService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/compounds")
@CrossOrigin(origins = {"http://localhost:3000", "https://localhost:3000"}) // Allow frontend requests

public class CompoundController {
    private final CompoundService compoundService;

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
                    request.getFormulaTypeInt(),
                    request.getDatabases(),
                    request.getMetaboliteType()
            );

            RTSearchResponseDTO result = compoundService.findCompoundsByMz(simpleRequest);
            response.getMSFeatures().addAll(result.getMSFeatures());
        }

        return response;
    }

}

