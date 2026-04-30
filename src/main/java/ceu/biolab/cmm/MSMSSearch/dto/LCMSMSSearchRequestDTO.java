package ceu.biolab.cmm.MSMSSearch.dto;

import ceu.biolab.cmm.shared.domain.ExperimentParameters;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class LCMSMSSearchRequestDTO extends MSMSSearchRequestDTO {
    @NotNull
    private Double rtValue;

    @Valid
    private ExperimentParameters experimentParameters;

    public Double getRtValue() {
        return rtValue;
    }

    public void setRtValue(Double rtValue) {
        this.rtValue = rtValue;
    }

    public ExperimentParameters getExperimentParameters() {
        return experimentParameters;
    }

    public void setExperimentParameters(ExperimentParameters experimentParameters) {
        this.experimentParameters = experimentParameters;
    }
}
