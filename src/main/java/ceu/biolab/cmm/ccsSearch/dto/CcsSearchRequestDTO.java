package ceu.biolab.cmm.ccsSearch.dto;

import ceu.biolab.cmm.ccsSearch.domain.BufferGas;
import ceu.biolab.cmm.ccsSearch.domain.CcsToleranceMode;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class CcsSearchRequestDTO {
    @NotEmpty
    private List<@NotNull @Positive Double> mzValues = new ArrayList<>();

    @Positive
    private double mzTolerance;

    @NotNull
    private MzToleranceMode mzToleranceMode;

    @NotEmpty
    private List<@NotNull @Positive Double> ccsValues = new ArrayList<>();

    @Positive
    private double ccsTolerance;

    @NotNull
    private CcsToleranceMode ccsToleranceMode;

    @NotNull
    private IonizationMode ionizationMode;

    @NotNull
    private BufferGas bufferGas;

    @NotEmpty
    private List<@NotBlank String> adducts = new ArrayList<>();

    public CcsSearchRequestDTO(List<Double> mzValues,
                               double mzTolerance,
                               MzToleranceMode mzToleranceMode,
                               List<Double> ccsValues,
                               double ccsTolerance,
                               CcsToleranceMode ccsToleranceMode,
                               IonizationMode ionizationMode,
                               BufferGas bufferGas,
                               List<String> adducts) {
        this.mzValues = mzValues != null ? new ArrayList<>(mzValues) : new ArrayList<>();
        this.mzTolerance = mzTolerance;
        this.mzToleranceMode = mzToleranceMode;
        this.ccsValues = ccsValues != null ? new ArrayList<>(ccsValues) : new ArrayList<>();
        this.ccsTolerance = ccsTolerance;
        this.ccsToleranceMode = ccsToleranceMode;
        this.ionizationMode = ionizationMode;
        this.bufferGas = bufferGas;
        this.adducts = adducts != null ? new ArrayList<>(adducts) : new ArrayList<>();
    }

    public void setMzValues(List<Double> mzValues) {
        this.mzValues = mzValues != null ? new ArrayList<>(mzValues) : null;
    }

    public void setMzToleranceMode(MzToleranceMode mzToleranceMode) {
        this.mzToleranceMode = mzToleranceMode;
    }

    public void setCcsValues(List<Double> ccsValues) {
        this.ccsValues = ccsValues != null ? new ArrayList<>(ccsValues) : null;
    }

    public void setCcsToleranceMode(CcsToleranceMode ccsToleranceMode) {
        this.ccsToleranceMode = ccsToleranceMode;
    }

    public void setIonizationMode(IonizationMode ionizationMode) {
        this.ionizationMode = ionizationMode;
    }

    public void setBufferGas(BufferGas bufferGas) {
        this.bufferGas = bufferGas;
    }

    public void setAdducts(List<String> adducts) {
        this.adducts = adducts != null ? new ArrayList<>(adducts) : null;
    }
}
