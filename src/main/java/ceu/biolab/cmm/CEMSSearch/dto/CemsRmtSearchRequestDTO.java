package ceu.biolab.cmm.CEMSSearch.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import ceu.biolab.cmm.CEMSMarkers.domain.MassMode;
import ceu.biolab.cmm.CEMSSearch.domain.CePolarity;
import ceu.biolab.cmm.CEMSSearch.domain.RmtToleranceMode;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CemsRmtSearchRequestDTO {

    private List<Double> masses = new ArrayList<>();

    private double tolerance;

    private MzToleranceMode toleranceMode = MzToleranceMode.PPM;

    @JsonProperty("rmt")
    private List<Double> relativeMigrationTimes = new ArrayList<>();

    @JsonProperty("rmt_tolerance")
    private double rmtTolerance;

    private RmtToleranceMode rmtToleranceMode = RmtToleranceMode.PERCENTAGE;

    @JsonProperty("buffer")
    private String bufferCode;

    private Double temperature;

    private CePolarity polarity = CePolarity.DIRECT;

    @JsonProperty("rmt_reference")
    private String rmtReference;

    @JsonProperty("chemical_alphabet")
    private String chemicalAlphabet;

    private IonizationMode ionMode = IonizationMode.POSITIVE;

    private MassMode massMode = MassMode.MZ;

    private List<String> adducts = new ArrayList<>();

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

    @JsonProperty("mass_mode")
    public void setMassMode(String value) {
        this.massMode = MassMode.fromValue(value);
    }
}
