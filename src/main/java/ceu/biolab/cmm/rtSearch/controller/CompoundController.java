package ceu.biolab.cmm.rtSearch.controller;

import ceu.biolab.cmm.rtSearch.dto.CompoundBatchSearchRequestDTO;
import ceu.biolab.cmm.rtSearch.dto.CompoundSimpleSearchRequestDTO;
import ceu.biolab.cmm.rtSearch.dto.RTSearchResponseDTO;
import ceu.biolab.cmm.rtSearch.service.CompoundService;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor; // Add this import

@RestController
@RequestMapping("/api/compounds")
@CrossOrigin(origins = {"http://localhost:3000", "https://localhost:3000"})
@RequiredArgsConstructor // Add this annotation
public class CompoundController {
    private final CompoundService compoundService;

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
            response.getMsFeatures().addAll(result.getMsFeatures());
        }

        return response;
    }

}

