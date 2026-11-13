package ceu.biolab.cmm.legacyApi.controller;

import ceu.biolab.cmm.legacyApi.dto.*;
import ceu.biolab.cmm.legacyApi.service.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class LegacyMsSearchController {
    private final LegacyMsSearchService ms;
    private final LegacyAdvancedSearchService advanced;
    public LegacyMsSearchController(LegacyMsSearchService ms, LegacyAdvancedSearchService advanced) { this.ms = ms; this.advanced = advanced; }
    @PostMapping("/simplesearch")
    public LegacyResults simplesearch(@Valid @RequestBody LegacySimpleRequest input) { return ms.simple(input); }
    @PostMapping("/browsesearch")
    public LegacyResults browsesearch(@Valid @RequestBody LegacyBrowseRequest input) { return ms.browse(input); }
    @PostMapping("/msmssearch")
    public LegacyResults msmssearch(@Valid @RequestBody LegacyMsmsRequest input) { return ms.msms(input); }
    @PostMapping("/advancedsearch")
    public LegacyResults advancedsearch(@Valid @RequestBody LegacyAdvancedRequest input) { return advanced.single(input); }
    @PostMapping("/advancedbatch")
    public LegacyResults advancedbatch(@Valid @RequestBody LegacyAdvancedBatchRequest input) { return advanced.batch(input, false); }
    @PostMapping("/v3/batch")
    public LegacyResults v3_batch(@Valid @RequestBody LegacyBatchRequest input) { return ms.batch(input); }
    @PostMapping("/v3/advancedbatch")
    public LegacyResults v3_advancedbatch(@Valid @RequestBody LegacyAdvancedBatchRequest input) { return advanced.batch(input, true); }
}
