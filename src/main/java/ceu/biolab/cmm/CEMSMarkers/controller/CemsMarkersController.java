package ceu.biolab.cmm.CEMSMarkers.controller;

import ceu.biolab.cmm.CEMSMarkers.dto.CemsMarkersRequestDTO;
import ceu.biolab.cmm.CEMSMarkers.dto.CemsMarkersTwoRequestDTO;
import ceu.biolab.cmm.CEMSMarkers.service.Cems1MarkerService;
import ceu.biolab.cmm.CEMSMarkers.service.Cems2MarkerService;
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

    public CemsMarkersController(Cems1MarkerService cems1MarkerService,
                                 Cems2MarkerService cems2MarkerService) {
        this.cems1MarkerService = cems1MarkerService;
        this.cems2MarkerService = cems2MarkerService;
    }

    @PostMapping("/CEMS1Marker")
    public CemsSearchResponseDTO search(@Valid @RequestBody CemsMarkersRequestDTO request) {
        return cems1MarkerService.search(request);
    }

    @PostMapping("/CEMS2Marker")
    public CemsSearchResponseDTO searchTwoMarkers(@Valid @RequestBody CemsMarkersTwoRequestDTO request) {
        return cems2MarkerService.search(request);
    }
}
