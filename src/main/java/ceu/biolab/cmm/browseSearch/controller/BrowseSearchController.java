package ceu.biolab.cmm.browseSearch.controller;

import ceu.biolab.cmm.browseSearch.dto.BrowseQueryResponse;
import ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest;
import ceu.biolab.cmm.browseSearch.service.BrowseSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class BrowseSearchController {
    @Autowired
    private BrowseSearchService browseSearchService;

    @PostMapping("/browseSearch")
    public BrowseQueryResponse search(@Valid @RequestBody BrowseSearchRequest request) {
        return browseSearchService.search(request);
    }
}
