package ceu.biolab.cmm.gcmsSearch.dto;

import ceu.biolab.cmm.gcmsSearch.domain.ColumnType;
import ceu.biolab.cmm.gcmsSearch.domain.DerivatizationMethod;
import lombok.Data;
import lombok.experimental.SuperBuilder;


//IT CONTAINS THE INFO THAT I USE TO FIND THE INFO THAT I WANT OF THE DATABASE
@Data //set, get,...
@SuperBuilder //constructor //usar en el resto de clases relacionadas con esta!!
public class GCMSFeatureQueryDTO {
    //private List<Peak> peaks;
    //private double RI;

    private double minRI;
    private double maxRI;
    private DerivatizationMethod derivatizationMethod;
    private ColumnType columnType;


}
