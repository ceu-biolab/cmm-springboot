package ceu.biolab.cmm.CEMSSearch.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import ceu.biolab.cmm.CEMSSearch.domain.CePolarity;
import ceu.biolab.cmm.CEMSSearch.domain.EffMobToleranceMode;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import lombok.Data;

@Data
public class CemsSearchRequestDTO {

    @JsonProperty("buffer_code")
    @NotBlank
    private String bufferCode;

    @NotNull
    private CePolarity polarity;

    @JsonProperty("chemical_alphabet")
    private String chemicalAlphabet;

    @NotNull
    private IonizationMode ionizationMode;

    @NotEmpty
    private List<@NotBlank String> adducts;

    @JsonProperty("mz_values")
    @NotEmpty
    private List<@NotNull @Positive Double> mzValues;

    @JsonProperty("effective_mobilities")
    @NotEmpty
    private List<@NotNull @Positive Double> effectiveMobilities;

    @JsonProperty("mz_tolerance")
    @Positive
    private double mzTolerance;

    @NotNull
    private MzToleranceMode mzToleranceMode;

    @JsonProperty("eff_mob_tolerance")
    @Positive
    private double effectiveMobilityTolerance;

    @NotNull
    @Positive
    private Double temperature;

    @NotNull
    private EffMobToleranceMode effectiveMobilityToleranceMode = EffMobToleranceMode.PERCENTAGE;

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

    @JsonProperty("eff_mob_tolerance_mode")
    public void setEffMobToleranceMode(String value) {
        this.effectiveMobilityToleranceMode = EffMobToleranceMode.fromValue(value);
    }
}
