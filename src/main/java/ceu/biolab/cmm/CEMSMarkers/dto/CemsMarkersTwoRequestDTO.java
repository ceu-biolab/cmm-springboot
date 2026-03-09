package ceu.biolab.cmm.CEMSMarkers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import ceu.biolab.cmm.CEMSMarkers.domain.MtToleranceMode;
import ceu.biolab.cmm.CEMSSearch.domain.CePolarity;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;

@Data
public class CemsMarkersTwoRequestDTO {

    @NotEmpty
    private List<@NotNull @Positive Double> masses = new ArrayList<>();

    @Positive
    private double tolerance;

    @NotNull
    private MzToleranceMode toleranceMode = MzToleranceMode.PPM;

    @JsonProperty("mt")
    @NotEmpty
    private List<@NotNull @Positive Double> migrationTimes = new ArrayList<>();

    @JsonProperty("mt_tolerance")
    @Positive
    private double migrationTimeTolerance;

    @NotNull
    private MtToleranceMode mtToleranceMode = MtToleranceMode.PERCENTAGE;

    @NotBlank
    private String buffer;

    @NotNull
    @Positive
    private Double temperature;

    @NotNull
    private CePolarity polarity = CePolarity.DIRECT;

    @JsonProperty("marker1")
    @NotBlank
    private String marker1;

    @JsonProperty("marker1_time")
    @Positive
    private double marker1Time;

    @JsonProperty("marker2")
    @NotBlank
    private String marker2;

    @JsonProperty("marker2_time")
    @Positive
    private double marker2Time;

    @JsonProperty("chemical_alphabet")
    private String chemicalAlphabet;

    @NotNull
    private IonizationMode ionMode = IonizationMode.POSITIVE;

    @NotEmpty
    private List<@NotBlank String> adducts = new ArrayList<>();

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

    @JsonProperty("mt_tolerance_mode")
    public void setMtToleranceMode(String value) {
        if (value == null) {
            throw new IllegalArgumentException("mt_tolerance_mode cannot be null");
        }
        this.mtToleranceMode = switch (value.trim().toLowerCase()) {
            case "percentage", "percent", "%" -> MtToleranceMode.PERCENTAGE;
            case "absolute" -> MtToleranceMode.ABSOLUTE;
            default -> throw new IllegalArgumentException("Unsupported mt_tolerance_mode: " + value);
        };
    }

    @JsonProperty("polarity")
    public void setPolarity(String value) {
        if (value == null) {
            throw new IllegalArgumentException("polarity cannot be null");
        }
        this.polarity = CePolarity.fromValue(value);
    }

    @JsonProperty("ion_mode")
    public void setIonMode(String value) {
        if (value == null) {
            throw new IllegalArgumentException("ion_mode cannot be null");
        }
        this.ionMode = IonizationMode.valueOf(value.trim().toUpperCase());
    }

}
