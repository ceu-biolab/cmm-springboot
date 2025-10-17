package ceu.biolab.cmm.MSMSSearch.dto;

import ceu.biolab.cmm.MSMSSearch.domain.MSMSAnnotation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MSMSSearchResponseDTO {
    private List<MSMSAnnotation> msmsList;

}
