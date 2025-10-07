package ceu.biolab.cmm.CEMSSearch.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchRequestDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchResponseDTO;
import ceu.biolab.cmm.CEMSSearch.service.CemsSearchService;

@RestController
@RequestMapping("/api")
public class CemsSearchController {

    private final CemsSearchService service;

    public CemsSearchController(CemsSearchService service) {
        this.service = service;
    }

    @PostMapping("/CEMSSearch")
    public CemsSearchResponseDTO search(@RequestBody CemsSearchRequestDTO request) {
        return service.search(request);
    }
}
