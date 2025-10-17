package ceu.biolab.cmm.CEMSMarkers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import ceu.biolab.cmm.CEMSMarkers.domain.MtToleranceMode;
import ceu.biolab.cmm.CEMSSearch.domain.CePolarity;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;

@Data
public class CemsMarkersRequestDTO {

    private List<Double> masses = new ArrayList<>();

    private double tolerance;

    private MzToleranceMode toleranceMode = MzToleranceMode.PPM;

    @JsonProperty("mt")
    private List<Double> migrationTimes = new ArrayList<>();

    @JsonProperty("mt_tolerance")
    private double migrationTimeTolerance;

    private MtToleranceMode mtToleranceMode = MtToleranceMode.PERCENTAGE;

    private String buffer;

    private Double temperature;

    private CePolarity polarity = CePolarity.DIRECT;

    private String marker;

    @JsonProperty("marker_time")
    private double markerTime;

    @JsonProperty("capillary_length")
    private double capillaryLength;

    @JsonProperty("capillary_voltage")
    private double capillaryVoltage;

    @JsonProperty("chemical_alphabet")
    private String chemicalAlphabet;

    private IonizationMode ionMode = IonizationMode.POSITIVE;

    private List<String> adducts = new ArrayList<>();

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
