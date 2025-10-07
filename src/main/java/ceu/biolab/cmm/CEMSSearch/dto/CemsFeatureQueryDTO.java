package ceu.biolab.cmm.CEMSSearch.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CemsFeatureQueryDTO {
    double massLower;
    double massUpper;
    double mobilityLower;
    double mobilityUpper;
    String bufferCode;
    int polarityId;
    int ionizationModeId;
}
