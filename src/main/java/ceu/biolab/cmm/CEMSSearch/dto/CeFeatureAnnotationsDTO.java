package ceu.biolab.cmm.CEMSSearch.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CeFeatureAnnotationsDTO {
    private CeFeatureDTO feature;
    private List<CeAnnotationsByAdductDTO> annotationsByAdducts;

    public CeFeatureAnnotationsDTO() {
        this.annotationsByAdducts = new ArrayList<>();
    }

    public void addAnnotationsByAdduct(CeAnnotationsByAdductDTO annotations) {
        this.annotationsByAdducts.add(annotations);
    }
}
