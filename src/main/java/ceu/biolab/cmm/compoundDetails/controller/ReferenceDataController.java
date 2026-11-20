package ceu.biolab.cmm.compoundDetails.controller;

import ceu.biolab.cmm.compoundDetails.dto.*;
import ceu.biolab.cmm.compoundDetails.service.CompoundDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReferenceDataController {
    private final CompoundDetailsService service;
    public ReferenceDataController(CompoundDetailsService service) { this.service = service; }

    @GetMapping("/classyfire/{nodeId}")
    public ClassyfireNodeResponse classyfire(@PathVariable String nodeId) { return service.classyfire(nodeId); }
    @GetMapping("/ontology/{termId}")
    public OntologyTermResponse ontology(@PathVariable long termId) { return service.ontology(termId); }
    @GetMapping("/references/{referenceId}")
    public ReferenceResponse reference(@PathVariable long referenceId) { return service.reference(referenceId); }
    @GetMapping("/organisms/{organismId}")
    public OrganismResponse organism(@PathVariable long organismId) { return service.organism(organismId); }
}
