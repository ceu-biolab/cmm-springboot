package ceu.biolab.cmm.CEMSMarkers.controller;

import ceu.biolab.cmm.CEMSMarkers.dto.CemsMarkersRequestDTO;
import ceu.biolab.cmm.CEMSMarkers.dto.CemsMarkersTwoRequestDTO;
import ceu.biolab.cmm.CEMSMarkers.dto.CemsRmtMarkersRequestDTO;
import ceu.biolab.cmm.CEMSMarkers.dto.CemsRmtMarkersTwoRequestDTO;
import ceu.biolab.cmm.CEMSMarkers.service.Cems1MarkerService;
import ceu.biolab.cmm.CEMSMarkers.service.Cems2MarkerService;
import ceu.biolab.cmm.CEMSMarkers.service.CemsRmt1MarkerService;
import ceu.biolab.cmm.CEMSMarkers.service.CemsRmt2MarkerService;
import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchResponseDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class CemsMarkersController {

    private final Cems1MarkerService cems1MarkerService;
    private final Cems2MarkerService cems2MarkerService;
    private final CemsRmt1MarkerService cemsRmt1MarkerService;
    private final CemsRmt2MarkerService cemsRmt2MarkerService;

    public CemsMarkersController(Cems1MarkerService cems1MarkerService,
                                 Cems2MarkerService cems2MarkerService,
                                 CemsRmt1MarkerService cemsRmt1MarkerService,
                                 CemsRmt2MarkerService cemsRmt2MarkerService) {
        this.cems1MarkerService = cems1MarkerService;
        this.cems2MarkerService = cems2MarkerService;
        this.cemsRmt1MarkerService = cemsRmt1MarkerService;
        this.cemsRmt2MarkerService = cemsRmt2MarkerService;
    }

    @PostMapping({"/cems-1-marker", "/CEMS1Marker"})
    public CemsSearchResponseDTO search(@Valid @RequestBody CemsMarkersRequestDTO request) {
        return cems1MarkerService.search(request);
    }

    @PostMapping({"/cems-2-marker", "/CEMS2Marker"})
    public CemsSearchResponseDTO searchTwoMarkers(@Valid @RequestBody CemsMarkersTwoRequestDTO request) {
        return cems2MarkerService.search(request);
    }

    @PostMapping({"/cems-rmt-1-marker", "/CEMSRMT1Marker"})
    public CemsSearchResponseDTO searchRmtOneMarker(@Valid @RequestBody CemsRmtMarkersRequestDTO request) {
        return cemsRmt1MarkerService.search(request);
    }

    @PostMapping({"/cems-rmt-2-marker", "/CEMSRMT2Marker"})
    public CemsSearchResponseDTO searchRmtTwoMarkers(@Valid @RequestBody CemsRmtMarkersTwoRequestDTO request) {
        return cemsRmt2MarkerService.search(request);
    }
}
