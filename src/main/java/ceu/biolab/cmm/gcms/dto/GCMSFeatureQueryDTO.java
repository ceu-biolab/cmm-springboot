package ceu.biolab.cmm.gcms.dto;

import ceu.biolab.cmm.gcms.domain.ColumnType;
import ceu.biolab.cmm.gcms.domain.DerivatizationMethod;
import ceu.biolab.cmm.shared.domain.msFeature.Peak;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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
