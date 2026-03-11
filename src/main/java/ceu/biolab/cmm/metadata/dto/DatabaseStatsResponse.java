package ceu.biolab.cmm.metadata.dto;

public record DatabaseStatsResponse(
        long compounds,
        long gcmsSpectra,
        long msmsSpectra,
        long ccsRecords,
        long cemsRecords
) {
}
