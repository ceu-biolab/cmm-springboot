package ceu.biolab.cmm.metadata.controller;

import ceu.biolab.cmm.metadata.dto.AdductCatalogResponse;
import ceu.biolab.cmm.metadata.dto.CeMsBufferOption;
import ceu.biolab.cmm.metadata.dto.CeMsOptionsResponse;
import ceu.biolab.cmm.metadata.dto.DatabaseStatsResponse;
import ceu.biolab.cmm.metadata.service.MetadataService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/metadata", "/api/get"})
public class MetadataController {

    private final MetadataService metadataService;

    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @GetMapping("/ccs-adducts")
    public AdductCatalogResponse getCcsAdducts() {
        return metadataService.getCcsAdductCatalog();
    }

    @GetMapping("/ce-ms-buffers")
    public List<CeMsBufferOption> getCeMsBuffers() {
        return metadataService.getCeMsBuffers();
    }

    @GetMapping("/ce-ms-options")
    public CeMsOptionsResponse getCeMsOptions(@RequestParam(required = false) String buffer,
                                              @RequestParam(required = false) Long temperature,
                                              @RequestParam(required = false) String polarity,
                                              @RequestParam(name = "ionization_mode", required = false) String ionizationMode) {
        return metadataService.getCeMsOptions(buffer, temperature, polarity, ionizationMode);
    }

    @GetMapping("/stats")
    public DatabaseStatsResponse getDatabaseStats() {
        return metadataService.getDatabaseStats();
    }
}
