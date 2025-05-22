package ceu.biolab.cmm.gcms.controller;

import ceu.biolab.cmm.ccsSearch.dto.CcsSearchRequestDTO;
import ceu.biolab.cmm.ccsSearch.dto.CcsSearchResponseDTO;
import ceu.biolab.cmm.ccsSearch.service.CcsSearchService;
import ceu.biolab.cmm.gcms.dto.GCMSSearchRequestDTO;
import ceu.biolab.cmm.gcms.service.GCMSSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

public class CompoundGCMSComtroller {
    @RestController
    @RequestMapping("/api")
    public class CompoundCcsController {

        @Autowired
        private GCMSSearchService compoundGCMSService; //TODO crearla y poner contenido

        @PostMapping("/gcms")
        public CcsSearchResponseDTO getCompoundsByRITolerance(@RequestBody GCMSSearchRequestDTO request) {
            if (request.getCcsValues() == null || request.getCcsValues().isEmpty()) {
                return new CcsSearchResponseDTO();
            }

            return compoundCcsService.search(request);
        }
    }

}
