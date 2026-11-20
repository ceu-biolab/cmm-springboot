package ceu.biolab.cmm.compoundDetails.dto;

import java.time.OffsetDateTime;

public record CompoundMetadata(OffsetDateTime created, OffsetDateTime lastUpdated) {}
