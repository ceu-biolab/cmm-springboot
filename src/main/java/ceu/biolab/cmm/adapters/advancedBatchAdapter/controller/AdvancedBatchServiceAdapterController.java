package ceu.biolab.cmm.adapters.advancedBatchAdapter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import ceu.biolab.cmm.adapters.advancedBatchAdapter.dto.AdvancedBatchAdapterRequestDTO;
import ceu.biolab.cmm.adapters.advancedBatchAdapter.dto.AdvancedBatchAdapterResponseDTO;
import ceu.biolab.cmm.adapters.advancedBatchAdapter.service.AdvancedBatchAdapterService;

/**
 * REST Controller for advanced batch search adapter endpoint.
 */

@RestController
@RequestMapping("/api")
public class AdvancedBatchServiceAdapterController {

    @Autowired
    private AdvancedBatchAdapterService advancedBatchAdapterService;

    @PostMapping("/advancedbatch")
    public AdvancedBatchAdapterResponseDTO advancedBatch(@Valid @RequestBody AdvancedBatchAdapterRequestDTO request) {
        return advancedBatchAdapterService.search(request);
    }
}
