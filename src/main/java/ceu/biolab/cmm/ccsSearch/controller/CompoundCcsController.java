package ceu.biolab.cmm.ccsSearch.controller;

import ceu.biolab.cmm.ccsSearch.dto.CcsSearchRequestDTO;
import ceu.biolab.cmm.ccsSearch.dto.CcsSearchResponseDTO;
import ceu.biolab.cmm.ccsSearch.dto.CcsScoringRequestDTO;
import ceu.biolab.cmm.ccsSearch.service.CcsSearchService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CompoundCcsController {

    @Autowired
    private CcsSearchService compoundCcsService;

    @PostMapping("/ccs")
    public CcsSearchResponseDTO getCompoundsByCcsTolerance(@Valid @RequestBody CcsSearchRequestDTO request) {
        return compoundCcsService.search(request);
    }

    @PostMapping("/ccs/lcms-score")
    public CcsSearchResponseDTO scoreCompoundsWithRetentionTime(@Valid @RequestBody CcsScoringRequestDTO request) {
        return compoundCcsService.searchWithLcmsScoring(request);
    }
}
