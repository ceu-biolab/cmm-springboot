package ceu.biolab.cmm.CEMSSearch.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ceu.biolab.cmm.CEMSSearch.domain.CePolarity;
import ceu.biolab.cmm.CEMSSearch.domain.RmtToleranceMode;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CemsRmtSearchRequestDTO {

    @NotEmpty
    private List<@NotNull @Positive Double> masses = new ArrayList<>();

    @Positive
    private double tolerance;

    @NotNull
    private MzToleranceMode toleranceMode = MzToleranceMode.PPM;

    @JsonProperty("rmt")
    @NotEmpty
    private List<@NotNull @Positive Double> relativeMigrationTimes = new ArrayList<>();

    @JsonProperty("rmt_tolerance")
    @Positive
    private double rmtTolerance;

    @NotNull
    private RmtToleranceMode rmtToleranceMode = RmtToleranceMode.PERCENTAGE;

    @JsonProperty("buffer")
    @NotBlank
    private String bufferCode;

    @NotNull
    @Positive
    private Double temperature;

    @NotNull
    private CePolarity polarity = CePolarity.DIRECT;

    @JsonProperty("rmt_reference")
    @NotBlank
    private String rmtReference;

    @JsonProperty("chemical_alphabet")
    private String chemicalAlphabet;

    @NotNull
    private IonizationMode ionMode = IonizationMode.POSITIVE;


    @NotEmpty
    private List<@NotBlank String> adducts = new ArrayList<>();

    @JsonProperty("ion_mode")
    public void setIonMode(String value) {
        if (value == null) {
            throw new IllegalArgumentException("ion_mode cannot be null");
        }
        this.ionMode = IonizationMode.valueOf(value.trim().toUpperCase());
    }

    @JsonProperty("polarity")
    public void setPolarity(String value) {
        if (value == null) {
            throw new IllegalArgumentException("polarity cannot be null");
        }
        this.polarity = CePolarity.fromValue(value);
    }

    @JsonProperty("tolerance_mode")
    public void setToleranceMode(String value) {
        if (value == null) {
            throw new IllegalArgumentException("tolerance_mode cannot be null");
        }
        this.toleranceMode = switch (value.trim().toLowerCase()) {
            case "ppm" -> MzToleranceMode.PPM;
            case "mda" -> MzToleranceMode.MDA;
            default -> throw new IllegalArgumentException("Unsupported tolerance_mode: " + value);
        };
    }

    @JsonProperty("rmt_tolerance_mode")
    public void setRmtToleranceMode(String value) {
        this.rmtToleranceMode = RmtToleranceMode.fromValue(value);
    }

}
