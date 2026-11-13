package ceu.biolab.cmm.compoundDetails.dto;

import java.time.OffsetDateTime;

public record ClassyfireNodeResponse(String nodeId, String nodeName, String kingdom, String superClass,
        String mainClass, String subClass, String level5, String level6, String level7,
        String level8, String level9, String level10, String directParent,
        OffsetDateTime created, OffsetDateTime lastUpdated) {}
