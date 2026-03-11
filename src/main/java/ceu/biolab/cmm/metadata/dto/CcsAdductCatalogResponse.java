package ceu.biolab.cmm.metadata.dto;

import java.util.List;

public record CcsAdductCatalogResponse(List<String> positive, List<String> negative) {
}
