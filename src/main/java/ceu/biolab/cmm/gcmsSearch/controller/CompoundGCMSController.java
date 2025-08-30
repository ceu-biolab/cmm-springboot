package ceu.biolab.cmm.gcmsSearch.controller;

import ceu.biolab.cmm.gcmsSearch.domain.ColumnType;
import ceu.biolab.cmm.gcmsSearch.domain.DerivatizationMethod;
import ceu.biolab.cmm.gcmsSearch.dto.GCMSSearchRequestDTO;
import ceu.biolab.cmm.gcmsSearch.dto.GCMSSearchResponseDTO;
import ceu.biolab.cmm.gcmsSearch.service.GCMSSearchService;
import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
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

        System.out.println("Datos recibidos controller: \n"+
                " s: "+request.getGcmsSpectrumExperimental() +
                "; \n ri: "+request.getRetentionIndex() +
                "; ritol: "+request.getRetentionIndexTolerance() +
                "; der: "+request.getDerivatizationMethod() +
                "; gccol: "+request.getColumnType());

        return compoundGCMSService.search(request);
    }
}


