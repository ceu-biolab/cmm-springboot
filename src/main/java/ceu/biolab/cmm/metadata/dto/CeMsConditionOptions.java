package ceu.biolab.cmm.metadata.dto;

import java.util.List;

public record CeMsConditionOptions(String key,
                                   CeMsBufferOption buffer,
                                   long temperature,
                                   String polarity,
                                   String ionizationMode,
                                   List<CeMsCompoundOption> markerCompounds,
                                   List<CeMsCompoundOption> rmtReferenceCompounds) {
}
