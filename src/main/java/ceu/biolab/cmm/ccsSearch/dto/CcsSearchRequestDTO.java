package ceu.biolab.cmm.ccsSearch.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ceu.biolab.cmm.ccsSearch.domain.BufferGas;
import ceu.biolab.cmm.ccsSearch.domain.CcsToleranceMode;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;

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
    
    public List<Double> getMzValues() {
        return mzValues;
    }

    public void setMzValues(List<Double> mzValues) {
        this.mzValues = mzValues != null ? mzValues : new ArrayList<>();
    }

    public double getMzTolerance() {
        return mzTolerance;
    }

    public void setMzTolerance(double mzTolerance) {
        if (mzTolerance < 0) {
            throw new IllegalArgumentException("mzTolerance must be non-negative");
        }
        this.mzTolerance = mzTolerance;
    }

    public MzToleranceMode getMzToleranceMode() {
        return mzToleranceMode;
    }

    public void setMzToleranceMode(MzToleranceMode mzToleranceMode) {
        this.mzToleranceMode = mzToleranceMode != null ? mzToleranceMode : MzToleranceMode.PPM;
    }

    public List<Double> getCcsValues() {
        return ccsValues;
    }

    public void setCcsValues(List<Double> ccsValues) {
        this.ccsValues = ccsValues != null ? ccsValues : new ArrayList<>();
    }

    public double getCcsTolerance() {
        return ccsTolerance;
    }

    public void setCcsTolerance(double ccsTolerance) {
        if (ccsTolerance < 0) {
            throw new IllegalArgumentException("ccsTolerance must be non-negative");
        }
        this.ccsTolerance = ccsTolerance;
    }

    public CcsToleranceMode getCcsToleranceMode() {
        return ccsToleranceMode;
    }

    public void setCcsToleranceMode(CcsToleranceMode ccsToleranceMode) {
        this.ccsToleranceMode = ccsToleranceMode != null ? ccsToleranceMode : CcsToleranceMode.PERCENTAGE;
    }

    public IonizationMode getionizationMode() {
        return ionizationMode;
    }

    public void setIonizationMode(IonizationMode ionizationMode) {
        this.ionizationMode = ionizationMode != null ? ionizationMode : IonizationMode.POSITIVE;
    }

    public BufferGas getBufferGas() {
        return bufferGas;
    }

    public void setBufferGas(BufferGas bufferGas) {
        this.bufferGas = bufferGas != null ? bufferGas : BufferGas.N2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CcsSearchRequestDTO that = (CcsSearchRequestDTO) o;
        return Double.compare(that.mzTolerance, mzTolerance) == 0 &&
               Double.compare(that.ccsTolerance, ccsTolerance) == 0 &&
               Objects.equals(mzValues, that.mzValues) &&
               mzToleranceMode == that.mzToleranceMode &&
               Objects.equals(ccsValues, that.ccsValues) &&
               ccsToleranceMode == that.ccsToleranceMode &&
               ionizationMode == that.ionizationMode &&
               bufferGas == that.bufferGas;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mzValues, mzTolerance, mzToleranceMode, ccsValues, ccsTolerance, ccsToleranceMode, ionizationMode, bufferGas);
    }

    @Override
    public String toString() {
        return "CcsSearchRequest{" +
                "mzValues=" + mzValues +
                ", mzTolerance=" + mzTolerance +
                ", mzToleranceMode=" + mzToleranceMode +
                ", ccsValues=" + ccsValues +
                ", ccsTolerance=" + ccsTolerance +
                ", ccsToleranceMode=" + ccsToleranceMode +
                ", ionizationMode=" + ionizationMode +
                ", bufferGas=" + bufferGas +
                '}';
    }
}
