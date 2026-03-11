package ceu.biolab.cmm.adducts.dto;

import java.util.List;

public record AdductCatalogResponse(List<String> positive, List<String> negative) {
}
