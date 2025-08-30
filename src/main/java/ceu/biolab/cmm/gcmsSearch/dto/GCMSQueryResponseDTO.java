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
    private FormulaType formulaType;
    private Double logP;
    private String casId;
    private int charge_type;
    private int charge_number;
    private int compound_type;
    private int compound_status;
    private int formula_type_int;

    private String inchi;
    private String inchiKey;
    private String smiles;

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
        this.formulaType = FormulaType.CHNOPS;

        this.logP = 0.0;
        this.casId = "";
        this.charge_type = -1;
        this.charge_number = -1;
        this.compound_type = -1;
        this.compound_status = -1;
        this.formula_type_int = -1;

        this.inchi = "";
        this.inchiKey = "";
        this.smiles = "";

        this.dertype = DerivatizationMethod.METHYL_CHLOROFORMATE;
        this.gcColumn = ColumnType.STANDARD_NON_POLAR;
        this.GCMSSpectrum = new ArrayList<>();
        this.RI = -1;
        //this.RT = -1;
    }


}
