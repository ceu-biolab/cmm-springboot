package ceu.biolab.cmm.CEMSSearch.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CemsRmtFeatureQueryDTO {
    double massLower;
    double massUpper;
    double rmtLower;
    double rmtUpper;
    String bufferCode;
    int polarityId;
    int ionizationModeId;
    long temperature;
    long referenceCompoundId;
}
