package ceu.biolab.cmm.MSMSSearch.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.msFeature.IScore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MSMSAnnotation {
    private int msmsId;
    private Compound compound;
    private String adduct;
    private SpectrumSource spectrumSource;
    private Double deltaPpmPrecursorIon;
    private Double msmsCosineScore;
    private Double collisionEnergy;
    private Spectrum spectrum;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<IScore> scores = new ArrayList<>();

    public void setScores(List<IScore> scores) {
        this.scores = scores == null ? new ArrayList<>() : new ArrayList<>(scores);
    }
}
