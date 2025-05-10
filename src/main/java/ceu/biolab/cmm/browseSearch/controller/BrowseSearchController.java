package ceu.biolab.cmm.browseSearch.controller;

import ceu.biolab.cmm.browseSearch.dto.BrowseQueryResponse;
import ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest;
import ceu.biolab.cmm.browseSearch.service.BrowseSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
    public class BrowseSearchController {

        @Autowired
        private BrowseSearchService browseSearchService;

        @PostMapping("/browse")
        public BrowseQueryResponse search(@RequestBody BrowseSearchRequest request) {
            return browseSearchService.search(request);
        }
    }


