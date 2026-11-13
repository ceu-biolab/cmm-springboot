package ceu.biolab.cmm.compoundDetails.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record OrganismResponse(Long organismId, String organismName, Long parentId, Integer organismLevel,
        OffsetDateTime created, OffsetDateTime lastUpdated,
        List<HierarchyEntry> organismHierarchy, List<EntityLink> compounds, List<EntityLink> references) {}
