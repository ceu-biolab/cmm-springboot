package ceu.biolab.cmm.CEMSSearch.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import ceu.biolab.cmm.CEMSSearch.domain.CePolarity;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import lombok.Data;

@Data
public class CemsSearchRequestDTO {

    @JsonProperty("buffer_code")
    private String bufferCode;

    private CePolarity polarity;

    @JsonProperty("chemical_alphabet")
    private String chemicalAlphabet;

    @JsonProperty("input_mass_mode")
    private String inputMassMode;

    private IonizationMode ionizationMode;

    private List<String> adducts;

    @JsonProperty("mz_values")
    private List<Double> mzValues;

    @JsonProperty("effective_mobilities")
    private List<Double> effectiveMobilities;

    @JsonProperty("mz_tolerance")
    private double mzTolerance;

    private MzToleranceMode mzToleranceMode;

    @JsonProperty("eff_mob_tolerance")
    private double effectiveMobilityTolerance;

    public CemsSearchRequestDTO() {
        this.adducts = new ArrayList<>();
        this.mzValues = new ArrayList<>();
        this.effectiveMobilities = new ArrayList<>();
        this.mzToleranceMode = MzToleranceMode.PPM;
        this.polarity = CePolarity.DIRECT;
        this.ionizationMode = IonizationMode.POSITIVE;
    }

    @JsonProperty("polarity")
    public void setPolarity(String value) {
        this.polarity = CePolarity.fromValue(value);
    }

    @JsonProperty("ionization_mode")
    public void setIonizationMode(String value) {
        this.ionizationMode = IonizationMode.valueOf(value.toUpperCase());
    }

    @JsonProperty("mz_tolerance_mode")
    public void setMzToleranceMode(String value) {
        this.mzToleranceMode = MzToleranceMode.valueOf(value.toUpperCase());
    }
}
