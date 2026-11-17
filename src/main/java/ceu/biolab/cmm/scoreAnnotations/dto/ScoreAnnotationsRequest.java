package ceu.biolab.cmm.scoreAnnotations.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import ceu.biolab.cmm.shared.domain.msFeature.ILCMSFeature;
import java.util.ArrayList;
import java.util.List;

import ceu.biolab.cmm.shared.domain.ExperimentParameters;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ScoreAnnotationsRequest {
    @NotEmpty
    private List<@NotNull @Valid AnnotatedFeature> features;
    private ExperimentParameters experimentParameters;

    @JsonIgnore
    @AssertTrue(message = "Scoring requires an LC feature with retention time for every entry")
    public boolean isLcmsFeatures() {
        return features == null || features.stream().allMatch(feature -> feature != null
                && feature.getFeature() instanceof ILCMSFeature);
    }

    public ScoreAnnotationsRequest() {
        this.features = new ArrayList<>();
    }

    public ScoreAnnotationsRequest(List<AnnotatedFeature> features, ExperimentParameters experimentParameters) {
        this.features = features;
        this.experimentParameters = experimentParameters;
    }
}
