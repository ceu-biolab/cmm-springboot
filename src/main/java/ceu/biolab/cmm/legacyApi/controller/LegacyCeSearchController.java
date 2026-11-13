package ceu.biolab.cmm.legacyApi.controller;

import ceu.biolab.cmm.legacyApi.dto.*;
import ceu.biolab.cmm.legacyApi.service.LegacyCeSearchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v3/cesearch")
public class LegacyCeSearchController {
    private final LegacyCeSearchService service;
    public LegacyCeSearchController(LegacyCeSearchService service) { this.service = service; }
    @PostMapping("/effmob")
    public LegacyResults.CeResults effmob(@Valid @RequestBody LegacyCeRequest input) { return service.search("effmob", input); }
    @PostMapping("/exprmt")
    public LegacyResults.CeResults exprmt(@Valid @RequestBody LegacyCeRequest input) { return service.search("exprmt", input); }
    @PostMapping("/mt1marker")
    public LegacyResults.CeResults mt1marker(@Valid @RequestBody LegacyCeRequest input) { return service.search("mt1marker", input); }
    @PostMapping("/mt2marker")
    public LegacyResults.CeResults mt2marker(@Valid @RequestBody LegacyCeRequest input) { return service.search("mt2marker", input); }
    @PostMapping("/rmt1marker")
    public LegacyResults.CeResults rmt1marker(@Valid @RequestBody LegacyCeRequest input) { return service.search("rmt1marker", input); }
    @PostMapping("/rmt2marker")
    public LegacyResults.CeResults rmt2marker(@Valid @RequestBody LegacyCeRequest input) { return service.search("rmt2marker", input); }
}
