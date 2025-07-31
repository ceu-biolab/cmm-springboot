package ceu.biolab.cmm.gcmsSearch.dto;

import ceu.biolab.cmm.gcmsSearch.domain.ColumnType;
import ceu.biolab.cmm.gcmsSearch.domain.DerivatizationMethod;
import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

//CONTAINS WHAT I HAVE FOUND ON THE DATA BASE --> GCMSCompound -> helps save the data to organize it here
@Data
@SuperBuilder
public class GCMSQueryResponseDTO {

    //TODO añadir resto info -> hecho
    private int compoundId;
    private String compoundName;
    private double monoisotopicMass;
    private String formula;
    FormulaType formulaType;
    Double logP;

    private DerivatizationMethod dertype;
    private ColumnType gcColumn;
    private List<Spectrum> GCMSSpectrum = null;

    private double RI;
    //private double RT;

    //GCMSCompound gcmsCompound;


    public GCMSQueryResponseDTO(int compoundId, String compoundName, double monoisotopicMass,
                                String formula, /*String formulaType, String compoundType, Double logP,
                                String pathwayName, Integer pathwayId, String pathwayMap,*/
                                DerivatizationMethod dertype, ColumnType gcColumn, List<Spectrum> GCMSSpectrum,
                                double RI, double RT) {
        this.compoundId = compoundId;
        this.compoundName = compoundName;
        this.monoisotopicMass = monoisotopicMass;
        this.formula = formula;
        this.dertype = dertype;
        this.gcColumn = gcColumn;
        this.GCMSSpectrum = GCMSSpectrum != null ? GCMSSpectrum : new ArrayList<>();
        this.RI = RI;
        //this.RT = RT;
    }

    public GCMSQueryResponseDTO() {
        this.compoundId = -1;
        this.compoundName = "";
        this.monoisotopicMass = 0.0;
        this.formula = "";
        this.dertype = DerivatizationMethod.METHYL_CHLOROFORMATE;
        this.gcColumn = ColumnType.STANDARD_NON_POLAR;
        this.GCMSSpectrum = new ArrayList<>();
        this.RI = -1;
        //this.RT = -1;
    }


}
