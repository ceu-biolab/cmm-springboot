package ceu.biolab.cmm.gcms.dto;

import ceu.biolab.cmm.gcms.domain.ColumnType;
import ceu.biolab.cmm.gcms.domain.DerivatizationMethod;
import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

//CONTAINS WHAT I HAVE FOUND ON THE DATA BASE --> GCMSCompound?
@Data
public class GCMSQueryResponseDTO {

    private int compoundId;
    private String compoundName;
    private double monoisotopicMass;
    private String formula;
    private String formulaType;
    private String compoundType; //en ccs esta como int con un to do para cambiar a string
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

    public GCMSQueryResponseDTO(int compoundId, String compoundName, double monoisotopicMass, double dbCcs,
                                String formula, String formulaType, String compoundType, Double logP,
                                String pathwayName, Integer pathwayId, String pathwayMap,
                                DerivatizationMethod dertype, ColumnType gcColumn, List<Spectrum> GCMS_Spectrum) {
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
        this.dertype = dertype;
        this.gcColumn = gcColumn;
        this.GCMS_Spectrum = GCMS_Spectrum != null ? GCMS_Spectrum : new ArrayList<>();
    }

    public GCMSQueryResponseDTO() {
        this.compoundId = -1;
        this.compoundName = "";
        this.monoisotopicMass = 0.0;
        this.formula = "";
        this.formulaType = "";
        this.compoundType = "";
        this.logP = null;  // Changed from 0.0 to null
        this.pathwayName = "";
        this.pathwayId = -1;
        this.pathwayMap = "";
        this.dertype = DerivatizationMethod.METHYL_CHLOROFORMATES;
        this.gcColumn = ColumnType.STANDARD_NON_POLAR;
        this.GCMS_Spectrum = new ArrayList<>();
    }


}
