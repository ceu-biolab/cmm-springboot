package ceu.biolab.cmm.compoundDetails.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ReferenceResponse(Long referenceId, String referenceText, String doi, String link,
        OffsetDateTime created, OffsetDateTime lastUpdated,
        List<EntityLink> compounds, List<EntityLink> organisms) {}
