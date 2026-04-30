package ceu.biolab.cmm.MSMSSearch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchResponseDTO;
import ceu.biolab.cmm.MSMSSearch.dto.LCMSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMSSearch.service.MSMSSearchService;

@RestController
@RequestMapping("/api")
public class MSMSSearchController {
    @Autowired
    private MSMSSearchService msmsSearchService;

    @PostMapping({"/msms-search", "/MSMSSearch"})
    public MSMSSearchResponseDTO search(@Valid @RequestBody MSMSSearchRequestDTO request) {
        return msmsSearchService.search(request);
    }

    @PostMapping("/lcmsms-search")
    public MSMSSearchResponseDTO searchWithLcmsScoring(@Valid @RequestBody LCMSMSSearchRequestDTO request) {
        return msmsSearchService.searchWithLcmsScoring(request);
    }
}
