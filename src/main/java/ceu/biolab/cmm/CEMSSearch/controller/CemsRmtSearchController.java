package ceu.biolab.cmm.CEMSSearch.controller;

import ceu.biolab.cmm.CEMSSearch.dto.CemsRmtSearchRequestDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchResponseDTO;
import ceu.biolab.cmm.CEMSSearch.service.CemsRmtSearchService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class CemsRmtSearchController {

    private final CemsRmtSearchService service;

    public CemsRmtSearchController(CemsRmtSearchService service) {
        this.service = service;
    }

    @PostMapping("/CEMSRMTSearch")
    public CemsSearchResponseDTO search(@Valid @RequestBody CemsRmtSearchRequestDTO request) {
        return service.search(request);
    }
}
