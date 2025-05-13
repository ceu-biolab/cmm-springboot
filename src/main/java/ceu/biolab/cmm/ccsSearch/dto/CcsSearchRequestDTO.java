package ceu.biolab.cmm.ccsSearch.dto;

import java.util.ArrayList;
import java.util.List;

import ceu.biolab.cmm.ccsSearch.domain.BufferGas;
import ceu.biolab.cmm.ccsSearch.domain.CcsToleranceMode;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import lombok.Data;

@Data
public class CcsSearchRequestDTO {
    private List<Double> mzValues;
    private double mzTolerance;
    private MzToleranceMode mzToleranceMode;
    private List<Double> ccsValues;
    private double ccsTolerance;
    private CcsToleranceMode ccsToleranceMode;
    private IonizationMode ionizationMode;
    private BufferGas bufferGas;

    public CcsSearchRequestDTO(List<Double> mzValues, double mzTolerance, MzToleranceMode mzToleranceMode,
                           List<Double> ccsValues, double ccsTolerance, CcsToleranceMode ccsToleranceMode,
                           IonizationMode ionizationMode) {
        this.mzValues = mzValues != null ? mzValues : new ArrayList<>();
        this.mzTolerance = mzTolerance;
        this.mzToleranceMode = mzToleranceMode != null ? mzToleranceMode : MzToleranceMode.PPM;
        this.ccsValues = ccsValues != null ? ccsValues : new ArrayList<>();
        this.ccsTolerance = ccsTolerance;
        this.ccsToleranceMode = ccsToleranceMode != null ? ccsToleranceMode : CcsToleranceMode.PERCENTAGE;
        this.ionizationMode = ionizationMode != null ? ionizationMode : IonizationMode.POSITIVE;
        this.bufferGas = bufferGas != null ? bufferGas : BufferGas.N2;
    }
}
