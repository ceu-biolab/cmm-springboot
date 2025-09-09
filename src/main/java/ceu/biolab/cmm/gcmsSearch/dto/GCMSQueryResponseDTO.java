package ceu.biolab.cmm.gcmsSearch.dto;

import ceu.biolab.cmm.gcmsSearch.domain.ColumnType;
import ceu.biolab.cmm.gcmsSearch.domain.DerivatizationMethod;
import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
public class GCMSQueryResponseDTO {

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

    public GCMSQueryResponseDTO(int compoundId, String compoundName, double monoisotopicMass,
                                String formula, DerivatizationMethod dertype, ColumnType gcColumn, List<Spectrum> GCMSSpectrum,
                                double RI) {
        this.compoundId = compoundId;
        this.compoundName = compoundName;
        this.monoisotopicMass = monoisotopicMass;
        this.formula = formula;
        this.dertype = dertype;
        this.gcColumn = gcColumn;
        this.GCMSSpectrum = GCMSSpectrum != null ? GCMSSpectrum : new ArrayList<>();
        this.RI = RI;
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
    }

}
