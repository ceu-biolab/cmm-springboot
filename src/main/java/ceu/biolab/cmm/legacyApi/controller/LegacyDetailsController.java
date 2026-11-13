package ceu.biolab.cmm.legacyApi.controller;

import ceu.biolab.cmm.compoundDetails.service.CompoundDetailsService;
import ceu.biolab.cmm.legacyApi.dto.LegacyDetailsRows.*;
import ceu.biolab.cmm.legacyApi.mapper.LegacyDetailsMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v3", produces = MediaType.APPLICATION_JSON_VALUE)
public class LegacyDetailsController {
    private final CompoundDetailsService service;
    private final LegacyDetailsMapper mapper;

    public LegacyDetailsController(CompoundDetailsService service, LegacyDetailsMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping("/compounds/{compoundId}")
    public CompoundInfo compound(@PathVariable int compoundId) {
        return mapper.compound(service.compound(compoundId));
    }
    @GetMapping("/classyfire/{nodeId}")
    public ClassyfireNode classyfire(@PathVariable String nodeId) {
        return mapper.classyfire(service.classyfire(nodeId));
    }
    @GetMapping("/ontology/{termId}")
    public OntologyTerm ontology(@PathVariable long termId) {
        return mapper.ontology(service.ontology(termId));
    }
    @GetMapping("/references/{referenceId}")
    public Reference reference(@PathVariable long referenceId) {
        return mapper.reference(service.reference(referenceId));
    }
    @GetMapping("/organisms/{organismId}")
    public Organism organism(@PathVariable long organismId) {
        return mapper.organism(service.organism(organismId));
    }
}
