package ceu.biolab.cmm.browseSearch.controller;

import ceu.biolab.cmm.browseSearch.dto.BrowseQueryResponse;
import ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest;
import ceu.biolab.cmm.browseSearch.service.BrowseSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "https://localhost:3000"}) // Allow frontend requests
public class BrowseSearchController {
    @Autowired
    private BrowseSearchService browseSearchService;

    @PostMapping("/browseSearch")
    public BrowseQueryResponse search(@RequestBody BrowseSearchRequest request) {
        return browseSearchService.search(request);
    }
}


