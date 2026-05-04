package ceu.biolab.cmm.metadata.dto;

import java.util.List;

public record AdductCatalogResponse(List<String> positive, List<String> negative) {
}
