package ceu.biolab.cmm.legacyApi.dto;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import ceu.biolab.cmm.legacyApi.validation.Finite;
import java.util.*;

@Data
public class LegacyAdvancedBatchRequest extends LegacySearchOptions {
    @NotEmpty private List<@NotNull @Positive @Finite Double> masses;
    @JsonProperty("retention_times") @NotEmpty private List<@NotNull @PositiveOrZero @Finite Double> retentionTimes;
    @JsonProperty("composite_spectra") private List<@NotNull List<@NotNull @Valid LegacyPeak>> compositeSpectra = List.of();
    @JsonProperty("all_masses") @NotNull private List<@NotNull @Positive @Finite Double> allMasses = List.of();
    @JsonProperty("all_retention_times") @NotNull private List<@NotNull @PositiveOrZero @Finite Double> allRetentionTimes = List.of();
    @JsonProperty("all_composite_spectra") private List<@NotNull List<@NotNull @Valid LegacyPeak>> allCompositeSpectra = List.of();
    @JsonProperty("chemical_alphabet") @NotBlank private String chemicalAlphabet = "CHNOPS";
    @NotNull private Boolean deuterium = false;
    @JsonProperty("modifiers_type") @NotBlank private String modifiersType = "none";
}
