package ceu.biolab.cmm.legacyApi.dto;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import ceu.biolab.cmm.legacyApi.validation.Finite;
import java.util.*;

@Data
public class LegacyBrowseRequest {
    private String name = "";
    private String formula = "";
    private boolean exactName;
    private boolean exactFormula;
    @NotEmpty private Set<@NotBlank String> databases = Set.of("all-except-mine");
    @JsonProperty("metabolites_type") @NotBlank private String metabolitesType = "all-except-peptides";
}
