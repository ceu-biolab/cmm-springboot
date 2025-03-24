package ceu.biolab.cmm.ccsSearch.controller;

import ceu.biolab.cmm.ccsSearch.dto.CcsSearchRequest;
import ceu.biolab.cmm.ccsSearch.dto.CcsSearchResponse;
import ceu.biolab.cmm.ccsSearch.service.CcsSearchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CompoundCcsController {

    @Autowired
    private CcsSearchService compoundCcsService;

    @PostMapping("/ccs")
    public CcsSearchResponse getCompoundsByCcsTolerance(@RequestBody CcsSearchRequest request) {
        if (request.getCcsValues() == null || request.getCcsValues().isEmpty()) {
            return new CcsSearchResponse();
        }
        
        return compoundCcsService.search(request);
    }
}
