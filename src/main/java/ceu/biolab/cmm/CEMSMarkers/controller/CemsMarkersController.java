package ceu.biolab.cmm.CEMSMarkers.controller;

import ceu.biolab.cmm.CEMSMarkers.dto.CemsMarkersRequestDTO;
import ceu.biolab.cmm.CEMSMarkers.service.CemsMarkersService;
import ceu.biolab.cmm.CEMSSearch.dto.CemsSearchResponseDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CemsMarkersController {

    private final CemsMarkersService service;

    public CemsMarkersController(CemsMarkersService service) {
        this.service = service;
    }

    @PostMapping("/CEMSMarkers")
    public CemsSearchResponseDTO search(@RequestBody CemsMarkersRequestDTO request) {
        return service.search(request);
    }
}
