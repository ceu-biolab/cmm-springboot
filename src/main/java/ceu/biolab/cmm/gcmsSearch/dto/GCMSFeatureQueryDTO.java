package ceu.biolab.cmm.gcmsSearch.dto;

import ceu.biolab.cmm.gcmsSearch.domain.ColumnType;
import ceu.biolab.cmm.gcmsSearch.domain.DerivatizationMethod;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class GCMSFeatureQueryDTO {
    private double minRI;
    private double maxRI;
    private DerivatizationMethod derivatizationMethod;
    private ColumnType columnType;

}
