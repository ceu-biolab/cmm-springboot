package ceu.biolab.cmm.gcmsSearch.dto;

import ceu.biolab.cmm.gcmsSearch.domain.ColumnType;
import ceu.biolab.cmm.gcmsSearch.domain.DerivatizationMethod;
import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.shared.domain.compound.CompoundType;
import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import lombok.Data;
import lombok.Builder.Default;
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
    private CompoundType compound_type;

    private String inchi;
    private String inchiKey;
    private String smiles;

    private String keggID;
    private String lmID;
    private String hmdbID;
    private String agilentID;
    private Integer pcID;
    private Integer chebiID;
    private String inHouseID;
    private Integer aspergillusID;
    private String knapsackID;
    private Integer npatlasID;
    private Integer fahfaID;
    private Integer ohPositionID;
    private String aspergillusWebName;

    private DerivatizationMethod dertype;
    private ColumnType gcColumn;
    @Default
    private List<Spectrum> GCMSSpectrum = new ArrayList<>();

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
        this.keggID = null;
        this.lmID = null;
        this.hmdbID = null;
        this.agilentID = null;
        this.pcID = null;
        this.chebiID = null;
        this.inHouseID = null;
        this.aspergillusID = null;
        this.knapsackID = null;
        this.npatlasID = null;
        this.fahfaID = null;
        this.ohPositionID = null;
        this.aspergillusWebName = null;
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
        this.compound_type = CompoundType.NON_LIPID;

        this.inchi = "";
        this.inchiKey = "";
        this.smiles = "";
        this.keggID = "";
        this.lmID = "";
        this.hmdbID = "";
        this.agilentID = "";
        this.pcID = null;
        this.chebiID = null;
        this.inHouseID = "";
        this.aspergillusID = null;
        this.knapsackID = "";
        this.npatlasID = null;
        this.fahfaID = null;
        this.ohPositionID = null;
        this.aspergillusWebName = "";

        this.dertype = DerivatizationMethod.METHYL_CHLOROFORMATE;
        this.gcColumn = ColumnType.STANDARD_NON_POLAR;
        this.GCMSSpectrum = new ArrayList<>();
        this.RI = -1;
    }

}
