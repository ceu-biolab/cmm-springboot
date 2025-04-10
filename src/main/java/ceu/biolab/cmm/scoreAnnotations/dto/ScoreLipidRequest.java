package ceu.biolab.cmm.scoreAnnotations.dto;

import java.util.List;

import ceu.biolab.cmm.shared.domain.ExperimentParameters;
import ceu.biolab.cmm.shared.domain.msFeature.AnnotatedFeature;
import lombok.Data;

@Data
public class ScoreLipidRequest {
    private List<AnnotatedFeature> features;
    private ExperimentParameters experimentParameters;
}
