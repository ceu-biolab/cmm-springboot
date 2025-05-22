package ceu.biolab.cmm.gcms.dto;

import ceu.biolab.cmm.gcms.domain.ColumnType;
import ceu.biolab.cmm.gcms.domain.DerivatizationMethod;
import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import lombok.Data;

import java.util.List;

//CONTAINS WHAT I HAVE FOUND ON THE DATA BASE --> GCMSCompound?
@Data
public class GCMSQueryResponseDTO {
    //private String compound_name;
    /*TODO poner toda info compounds!!!!
    private Integer pathwayId;
    private String pathwayName;
    private String pathwayMap;*/
    private int compoundId;
    private String compoundName;
    private double monoisotopicMass;
    private String formula;
    private String formulaType;
    private int compoundType;
    private Double logP;
    private Integer pathwayId;
    private String pathwayName;
    private String pathwayMap;

    private DerivatizationMethod dertype;
    private ColumnType gcColumn;
    private List<Spectrum> GCMS_Spectrum = null;

    //GCMSCompound gcmsCompound;

    /*public GCMSQueryResponseDTO(String compound_name, DerivationMethod dertype,
                                ColumnType gcColumn, List<Spectrum> GCMS_Spectrum) {
        this.compoundName = compound_name;
        this.dertype = dertype;
        this.gcColumn = gcColumn;

        this.GCMS_Spectrum = GCMS_Spectrum;
    }*/

    public GCMSQueryResponseDTO(int compoundId, String compoundName, double monoisotopicMass, double dbCcs, String formula, String formulaType, int compoundType, Double logP, String pathwayName, Integer pathwayId, String pathwayMap) {
        this.compoundId = compoundId;
        this.compoundName = compoundName;
        this.monoisotopicMass = monoisotopicMass;
        this.formula = formula;
        this.formulaType = formulaType;
        this.compoundType = compoundType;
        this.logP = logP;
        this.pathwayName = pathwayName;
        this.pathwayId = pathwayId;
        this.pathwayMap = pathwayMap;
    }

    public GCMSQueryResponseDTO() {
        this.compoundId = -1;
        this.compoundName = "";
        this.monoisotopicMass = 0.0;
        this.formula = "";
        this.formulaType = "";
        this.compoundType = -1;
        this.logP = null;  // Changed from 0.0 to null
        this.pathwayName = "";
        this.pathwayId = -1;
        this.pathwayMap = "";
    }


}
