package ceu.biolab.cmm.scoreAnnotations.dto;

import java.util.List;

import ceu.biolab.cmm.shared.domain.ExperimentParameters;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import lombok.Data;

@Data
public class ScoreLipidRequest {
    private List<AnnotatedFeature> features;
    private ExperimentParameters experimentParameters;

    public ScoreLipidRequest() {
        // default constructor required for Jackson
    }

    public ScoreLipidRequest(List<AnnotatedFeature> features, ExperimentParameters experimentParameters) {
        this.features = features;
        this.experimentParameters = experimentParameters;
    }
}
