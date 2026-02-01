package ceu.biolab.cmm.adapters.simpleSearchAdapter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import ceu.biolab.cmm.adapters.simpleSearchAdapter.dto.SimpleSearchAdapterRequestDTO;
import ceu.biolab.cmm.adapters.simpleSearchAdapter.dto.SimpleSearchAdapterResponseDTO;
import ceu.biolab.cmm.adapters.simpleSearchAdapter.service.SimpleSearchAdapterService;

/**
 * REST Controller for simple Search Adapter endpoint.
 * Adapts the previous simple search API to the new definition.
 * 
 * Request flow:
 * 1. Receives JSON POST request with @Valid validation
 * 2. Delegates to SimpleSearchService for processing
 * 3. Returns JSON response with search results
 */

@RestController
@RequestMapping("/api")
public class SimpleSearchServiceAdapterController {

    @Autowired
    private SimpleSearchAdapterService simpleSearchAdapterService;

    /**
     * Performs a search on a single compound.
     * 
     * @param request Valid simple search adapter request DTO
     * @return Response containing search results
     */
    @PostMapping("/simplesearch")
    public SimpleSearchAdapterResponseDTO batchSearch(@Valid @RequestBody SimpleSearchAdapterRequestDTO request) {
        return simpleSearchAdapterService.search(request);
    }
}
