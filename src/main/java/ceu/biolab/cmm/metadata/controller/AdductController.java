package ceu.biolab.cmm.metadata.controller;

import ceu.biolab.cmm.metadata.dto.AdductCatalogResponse;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.adduct.AdductDefinition;
import ceu.biolab.cmm.shared.service.adduct.AdductService;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdductController {

    @GetMapping({"/api/adducts", "/api/get/adducts", "/api/metadata/adducts"})
    public AdductCatalogResponse listAdducts() {
        return new AdductCatalogResponse(
                canonicalAdducts(IonizationMode.POSITIVE),
                canonicalAdducts(IonizationMode.NEGATIVE)
        );
    }

    private List<String> canonicalAdducts(IonizationMode mode) {
        List<AdductDefinition> ordered = AdductService.sortByPriority(
                new LinkedHashSet<>(AdductService.definitionMap(mode).values()), mode);
        return ordered.stream()
                .map(AdductDefinition::canonical)
                .toList();
    }
}
