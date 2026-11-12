package ceu.biolab.cmm.gcmsSearch.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import ceu.biolab.cmm.gcmsSearch.domain.GCMSAnnotation;
import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class GCMSSearchResponseDTO {
    private Spectrum gcmsSpectrumExperimental;
    private List<GCMSAnnotation> gcmsAnnotations = new ArrayList<>();
    @JsonProperty("riexperimental")
    private double riExperimental;

    public GCMSSearchResponseDTO(Spectrum gcmsSpectrumExperimental, List<GCMSAnnotation> gcmsAnnotations, double riExperimental) {
        this.gcmsSpectrumExperimental = gcmsSpectrumExperimental;
        this.gcmsAnnotations = gcmsAnnotations != null ? gcmsAnnotations : new ArrayList<>();
        this.riExperimental = riExperimental;
    }

    @Override
    public String toString() {
        return "GcmsSearchResponseDTO{" +
                "gcmsSpectrumExperimental=" + gcmsSpectrumExperimental +
                ", gcmsAnnotations=" + gcmsAnnotations +
                ", riExperimental=" + riExperimental +
                '}';
    }
}
