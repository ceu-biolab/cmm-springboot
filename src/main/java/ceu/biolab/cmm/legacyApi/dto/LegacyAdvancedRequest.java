package ceu.biolab.cmm.legacyApi.dto;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import ceu.biolab.cmm.legacyApi.validation.Finite;
import java.util.*;

@Data
public class LegacyAdvancedRequest extends LegacySimpleRequest {
    @JsonProperty("retention_time") @NotNull @PositiveOrZero @Finite private Double retentionTime;
    @JsonProperty("composite_spectrum") private List<@NotNull @Valid LegacyPeak> compositeSpectrum = List.of();
    @JsonProperty("chemical_alphabet") @NotBlank private String chemicalAlphabet = "CHNOPS";
    @NotNull private Boolean deuterium = false;
    @JsonProperty("modifiers_type") @NotBlank private String modifiersType = "none";
}
