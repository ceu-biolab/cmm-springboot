package ceu.biolab.cmm.scoreAnnotations.dto;

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
    private List<@Valid AnnotatedFeature> features;
    private ExperimentParameters experimentParameters;

    public ScoreAnnotationsRequest() {
        this.features = new ArrayList<>();
    }

    public ScoreAnnotationsRequest(List<AnnotatedFeature> features, ExperimentParameters experimentParameters) {
        this.features = features;
        this.experimentParameters = experimentParameters;
    }
}
