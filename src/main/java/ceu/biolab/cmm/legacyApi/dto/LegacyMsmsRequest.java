package ceu.biolab.cmm.legacyApi.dto;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import ceu.biolab.cmm.legacyApi.validation.Finite;
import java.util.*;

@Data
public class LegacyMsmsRequest {
    @JsonProperty("ion_mass") @NotNull @Positive @Finite private Double ionMass;
    @JsonProperty("ms_ms_peaks") @NotEmpty private List<@NotNull @Valid LegacyPeak> peaks;
    @JsonProperty("precursor_ion_tolerance") @NotNull @Positive @Finite private Double precursorTolerance = 10d;
    @JsonProperty("precursor_ion_tolerance_mode") @NotBlank private String precursorToleranceMode = "ppm";
    @JsonProperty("precursor_mz_tolerance") @NotNull @Positive @Finite private Double fragmentTolerance = 10d;
    @JsonProperty("precursor_mz_tolerance_mode") @NotBlank private String fragmentToleranceMode = "ppm";
    @JsonProperty("ion_mode") @NotBlank private String ionMode = "positive";
    @JsonProperty("ionization_voltage") @JsonAlias("Ionization_voltage") @NotBlank private String ionizationVoltage = "low";
    @JsonProperty("spectra_types") @NotEmpty private Set<@NotBlank String> spectraTypes = Set.of("experimental");
}
