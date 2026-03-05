package ceu.biolab.cmm.adapters.msmsSearchAdapter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import ceu.biolab.cmm.adapters.msmsSearchAdapter.dto.MsmsSearchAdapterRequestDTO;
import ceu.biolab.cmm.adapters.msmsSearchAdapter.dto.MsmsSearchAdapterResponseDTO;
import ceu.biolab.cmm.adapters.msmsSearchAdapter.service.MsmsSearchAdapterService;

/**
 * REST Controller for MS/MS search adapter endpoint.
 */

@RestController
@RequestMapping("/api")
public class MsmsSearchServiceAdapterController {

    @Autowired
    private MsmsSearchAdapterService msmsSearchAdapterService;

    @PostMapping("/msmssearch")
    public MsmsSearchAdapterResponseDTO msmsSearch(@Valid @RequestBody MsmsSearchAdapterRequestDTO request) {
        return msmsSearchAdapterService.search(request);
    }
}
