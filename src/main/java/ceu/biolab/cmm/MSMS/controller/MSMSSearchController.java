package ceu.biolab.cmm.MSMS.controller;

import ceu.biolab.cmm.MSMS.dto.MSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMS.dto.MSMSSearchResponseDTO;
import ceu.biolab.cmm.MSMS.service.MSMSSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MSMSSearchController {

    @Autowired
    private MSMSSearchService msmsSearchService;

    @PostMapping("/browseSearch")
    public MSMSSearchResponseDTO search(@RequestBody MSMSSearchRequestDTO request) {
        return msmsSearchService.search(request);
    }
}


