package ceu.biolab.cmm.legacyApi.dto;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import ceu.biolab.cmm.legacyApi.validation.Finite;
import java.util.*;

@Data
public class LegacyCeRequest extends LegacySearchOptions {
    @NotEmpty private List<@NotNull @Positive @Finite Double> masses;
    @JsonProperty("effective_mobilities") private List<@NotNull @Positive @Finite Double> effectiveMobilities;
    private List<@NotNull @Positive @Finite Double> rmt;
    private List<@NotNull @Positive @Finite Double> mt;
    @JsonProperty("eff_mob_tolerance") @Finite private Double effMobTolerance;
    @JsonProperty("eff_mob_tolerance_mode") private String effMobToleranceMode;
    @JsonProperty("rmt_tolerance") @Finite private Double rmtTolerance;
    @JsonProperty("rmt_tolerance_mode") private String rmtToleranceMode;
    @JsonProperty("mt_tolerance") @Finite private Double mtTolerance;
    @JsonProperty("mt_tolerance_mode") private String mtToleranceMode;
    @NotBlank private String buffer = "formic-acid-1m-20c";
    @NotBlank private String polarity = "direct";
    @JsonProperty("rmt_reference") private String rmtReference;
    private String marker;
    @JsonProperty("marker_time") @Finite private Double markerTime;
    private String marker2;
    @JsonProperty("marker2_time") @Finite private Double marker2Time;
    @JsonProperty("capillary_length") @Finite private Double capillaryLength;
    @JsonProperty("capillary_voltage") @Finite private Double capillaryVoltage;
    @JsonProperty("chemical_alphabet") @NotBlank private String chemicalAlphabet = "all";
    @NotNull private Boolean deuterium = false;
}
