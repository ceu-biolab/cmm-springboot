package ceu.biolab.cmm.compoundDetails.dto;

import java.time.OffsetDateTime;

public record OntologyTermResponse(Long ontologyTermId, String term, String definition, String externalId,
        String externalSource, String ontologyComment, String curator, Long parentId,
        Integer ontologyLevel, OffsetDateTime created, OffsetDateTime lastUpdated) {}
