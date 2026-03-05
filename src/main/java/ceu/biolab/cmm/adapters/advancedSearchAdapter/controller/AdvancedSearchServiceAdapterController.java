package ceu.biolab.cmm.adapters.advancedSearchAdapter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import ceu.biolab.cmm.adapters.advancedSearchAdapter.dto.AdvancedSearchAdapterRequestDTO;
import ceu.biolab.cmm.adapters.advancedSearchAdapter.dto.AdvancedSearchAdapterResponseDTO;
import ceu.biolab.cmm.adapters.advancedSearchAdapter.service.AdvancedSearchAdapterService;

/**
 * REST Controller for advanced search adapter endpoint.
 */

@RestController
@RequestMapping("/api")
public class AdvancedSearchServiceAdapterController {

    @Autowired
    private AdvancedSearchAdapterService advancedSearchAdapterService;

    @PostMapping("/advancedsearch")
    public AdvancedSearchAdapterResponseDTO advancedSearch(@Valid @RequestBody AdvancedSearchAdapterRequestDTO request) {
        return advancedSearchAdapterService.search(request);
    }
}
