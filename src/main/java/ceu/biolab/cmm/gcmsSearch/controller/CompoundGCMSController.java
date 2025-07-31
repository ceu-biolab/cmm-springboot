package ceu.biolab.cmm.gcmsSearch.controller;

import ceu.biolab.cmm.gcmsSearch.dto.GCMSSearchRequestDTO;
import ceu.biolab.cmm.gcmsSearch.dto.GCMSSearchResponseDTO;
import ceu.biolab.cmm.gcmsSearch.service.GCMSSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CompoundGCMSController {

    @Autowired
    private GCMSSearchService compoundGCMSService;

    @PostMapping("/gcms")
    public GCMSSearchResponseDTO getCompoundsByRITolerance(@RequestBody GCMSSearchRequestDTO request) {
        /*if (request.getGcmsSpectrum() == null || request.getGcmsSpectrum().isEmpty()) {
            return new GCMSSearchResponseDTO();
        }*/
        //if(request.getRetentionIndexTolerance())

        return compoundGCMSService.search(request);
    }
}


