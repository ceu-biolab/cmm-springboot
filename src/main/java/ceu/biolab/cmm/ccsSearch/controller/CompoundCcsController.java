package ceu.biolab.cmm.ccsSearch.controller;

import ceu.biolab.cmm.ccsSearch.dto.CcsSearchRequestDTO;
import ceu.biolab.cmm.ccsSearch.dto.CcsSearchResponseDTO;
import ceu.biolab.cmm.ccsSearch.service.CcsSearchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CompoundCcsController {

    @Autowired
    private CcsSearchService compoundCcsService;

    @PostMapping("/ccs")
    public CcsSearchResponseDTO getCompoundsByCcsTolerance(@RequestBody CcsSearchRequestDTO request) {
        if (request.getCcsValues() == null || request.getCcsValues().isEmpty()) {
            return new CcsSearchResponseDTO();
        }
        
        return compoundCcsService.search(request);
    }
}
