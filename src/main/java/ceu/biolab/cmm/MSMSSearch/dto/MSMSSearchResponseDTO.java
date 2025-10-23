package ceu.biolab.cmm.MSMSSearch.dto;

import ceu.biolab.cmm.MSMSSearch.domain.MSMSAnnotation;
import ceu.biolab.cmm.MSMSSearch.domain.Spectrum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class MSMSSearchResponseDTO {
    private List<MSMSAnnotation> msmsList = new ArrayList<>();
    private Spectrum experimentalSpectrum;

    public MSMSSearchResponseDTO(List<MSMSAnnotation> msmsList) {
        this.msmsList = msmsList;
    }
}
