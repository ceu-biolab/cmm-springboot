package ceu.biolab.cmm.compoundDetails.dto;

import java.time.OffsetDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import ceu.biolab.cmm.shared.domain.compound.Compound;

public record CompoundDetailsResponse(@JsonUnwrapped Compound compound, OffsetDateTime created, OffsetDateTime lastUpdated,
        List<EntityLink> reactions, List<EntityLink> classyfireClassification,
        List<EntityLink> ontologyTerms, List<EntityLink> references, List<EntityLink> organisms) {}
