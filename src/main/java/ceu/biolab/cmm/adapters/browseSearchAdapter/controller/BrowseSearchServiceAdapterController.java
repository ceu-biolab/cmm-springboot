package ceu.biolab.cmm.adapters.browseSearchAdapter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import ceu.biolab.cmm.adapters.browseSearchAdapter.dto.BrowseSearchAdapterRequestDTO;
import ceu.biolab.cmm.adapters.browseSearchAdapter.dto.BrowseSearchAdapterResponseDTO;
import ceu.biolab.cmm.adapters.browseSearchAdapter.service.BrowseSearchAdapterService;

/**
 * REST Controller for browse search adapter endpoint.
 */

@RestController
@RequestMapping("/api")
public class BrowseSearchServiceAdapterController {

    @Autowired
    private BrowseSearchAdapterService browseSearchAdapterService;

    @PostMapping("/browsesearch")
    public BrowseSearchAdapterResponseDTO browseSearch(@Valid @RequestBody BrowseSearchAdapterRequestDTO request) {
        return browseSearchAdapterService.search(request);
    }
}
