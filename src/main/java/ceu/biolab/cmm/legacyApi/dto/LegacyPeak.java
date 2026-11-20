package ceu.biolab.cmm.legacyApi.dto;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import ceu.biolab.cmm.legacyApi.validation.Finite;
import java.util.*;

@Data
public class LegacyPeak {
    @NotNull @Positive @Finite private Double mz;
    @NotNull @PositiveOrZero @Finite private Double intensity;
}
