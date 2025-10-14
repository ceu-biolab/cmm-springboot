package ceu.biolab.cmm.ccsSearch.dto;

import java.util.ArrayList;
import java.util.List;

import ceu.biolab.cmm.ccsSearch.domain.BufferGas;
import ceu.biolab.cmm.ccsSearch.domain.CcsToleranceMode;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import lombok.Data;

@Data
@lombok.NoArgsConstructor
public class CcsSearchRequestDTO {
    private List<Double> mzValues = new ArrayList<>();
    private double mzTolerance;
    private MzToleranceMode mzToleranceMode = MzToleranceMode.PPM;
    private List<Double> ccsValues = new ArrayList<>();
    private double ccsTolerance;
    private CcsToleranceMode ccsToleranceMode = CcsToleranceMode.PERCENTAGE;
    private IonizationMode ionizationMode = IonizationMode.POSITIVE;
    private BufferGas bufferGas = BufferGas.N2;
    private List<String> adducts = new ArrayList<>();

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
        this.mzToleranceMode = mzToleranceMode != null ? mzToleranceMode : MzToleranceMode.PPM;
        this.ccsValues = ccsValues != null ? new ArrayList<>(ccsValues) : new ArrayList<>();
        this.ccsTolerance = ccsTolerance;
        this.ccsToleranceMode = ccsToleranceMode != null ? ccsToleranceMode : CcsToleranceMode.PERCENTAGE;
        this.ionizationMode = ionizationMode != null ? ionizationMode : IonizationMode.POSITIVE;
        this.bufferGas = bufferGas != null ? bufferGas : BufferGas.N2;
        this.adducts = adducts != null ? new ArrayList<>(adducts) : new ArrayList<>();
    }

    public void setMzValues(List<Double> mzValues) {
        this.mzValues = mzValues != null ? new ArrayList<>(mzValues) : new ArrayList<>();
    }

    public void setMzToleranceMode(MzToleranceMode mzToleranceMode) {
        this.mzToleranceMode = mzToleranceMode != null ? mzToleranceMode : MzToleranceMode.PPM;
    }

    public void setCcsValues(List<Double> ccsValues) {
        this.ccsValues = ccsValues != null ? new ArrayList<>(ccsValues) : new ArrayList<>();
    }

    public void setCcsToleranceMode(CcsToleranceMode ccsToleranceMode) {
        this.ccsToleranceMode = ccsToleranceMode != null ? ccsToleranceMode : CcsToleranceMode.PERCENTAGE;
    }

    public void setIonizationMode(IonizationMode ionizationMode) {
        this.ionizationMode = ionizationMode != null ? ionizationMode : IonizationMode.POSITIVE;
    }

    public void setBufferGas(BufferGas bufferGas) {
        this.bufferGas = bufferGas != null ? bufferGas : BufferGas.N2;
    }

    public void setAdducts(List<String> adducts) {
        this.adducts = adducts != null ? new ArrayList<>(adducts) : new ArrayList<>();
    }
}
