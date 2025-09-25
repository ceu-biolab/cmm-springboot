package ceu.biolab.cmm.CEMSSearch.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CeFeatureDTO {
    double mzValue;
    double effectiveMobility;
    Double intensity;
}
