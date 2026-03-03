package ceu.biolab.cmm.metadata.controller;

import ceu.biolab.cmm.metadata.dto.CcsAdductCatalogResponse;
import ceu.biolab.cmm.metadata.dto.CeMsBufferOption;
import ceu.biolab.cmm.metadata.dto.DatabaseStatsResponse;
import ceu.biolab.cmm.metadata.service.MetadataService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/get")
public class MetadataController {

    private final MetadataService metadataService;

    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @GetMapping("/ccs-adducts")
    public CcsAdductCatalogResponse getCcsAdducts() {
        return metadataService.getCcsAdductCatalog();
    }

    @GetMapping("/ce-ms-buffers")
    public List<CeMsBufferOption> getCeMsBuffers() {
        return metadataService.getCeMsBuffers();
    }

    @GetMapping("/stats")
    public DatabaseStatsResponse getDatabaseStats() {
        return metadataService.getDatabaseStats();
    }
}
