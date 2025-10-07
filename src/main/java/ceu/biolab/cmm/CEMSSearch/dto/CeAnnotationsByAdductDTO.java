package ceu.biolab.cmm.CEMSSearch.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CeAnnotationsByAdductDTO {
    private String adduct;
    private List<CeAnnotationDTO> annotations;

    public CeAnnotationsByAdductDTO() {
        this.annotations = new ArrayList<>();
    }

    public CeAnnotationsByAdductDTO(String adduct) {
        this.adduct = adduct;
        this.annotations = new ArrayList<>();
    }

    public void addAnnotation(CeAnnotationDTO annotation) {
        this.annotations.add(annotation);
    }
}
