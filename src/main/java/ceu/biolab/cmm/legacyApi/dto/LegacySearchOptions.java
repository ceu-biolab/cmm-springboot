package ceu.biolab.cmm.legacyApi.dto;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import ceu.biolab.cmm.legacyApi.validation.Finite;
import java.util.*;

@Data
public class LegacySearchOptions {
    @NotNull @Positive @Finite private Double tolerance = 10d;
    @JsonProperty("tolerance_mode") @NotBlank private String toleranceMode = "ppm";
    @JsonProperty("ion_mode") @NotBlank private String ionMode = "positive";
    @JsonProperty("masses_mode") @NotBlank private String massesMode = "mz";
    @NotEmpty private List<@NotBlank String> adducts = List.of("all");
    @NotEmpty private Set<@NotBlank String> databases = Set.of("all-except-mine");
    @JsonProperty("metabolites_type") @NotBlank private String metabolitesType = "all-except-peptides";
}
