package ceu.biolab.cmm.MSMSSearch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchResponseDTO;
import ceu.biolab.cmm.MSMSSearch.service.MSMSSearchService;

@RestController
@RequestMapping("/api")
public class MSMSSearchController {
    @Autowired
    private MSMSSearchService msmsSearchService;

    @PostMapping("/MSMSSearch")
    public MSMSSearchResponseDTO search(@RequestBody MSMSSearchRequestDTO request) {
        return msmsSearchService.search(request);
    }
}


