package ceu.biolab.cmm.gcms.dto;

import ceu.biolab.cmm.gcms.domain.ColumnType;
import ceu.biolab.cmm.gcms.domain.DerivatizationMethod;
import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
// TODO LOMBOK
public class GCMSSearchRequestDTO {

    private List<Spectrum> gcmsSpectrum; //Group of Spectra

    private double mzTolerance; //Units Da_or_ppm
    private MzToleranceMode mzToleranceMode;
    private double retentionIndex;
    private double retentionIndexTolerance;

    private DerivatizationMethod derivatizationMethod;
    private ColumnType columnType;


    public GCMSSearchRequestDTO(List<Spectrum> gcmsSpectrum, double mzTolerance,
                                MzToleranceMode mzToleranceMode, double retentionIndex, double retentionIndexTolerance,
                                DerivatizationMethod derivatizationMethod, ColumnType columnType) {
        this.gcmsSpectrum = gcmsSpectrum != null ? gcmsSpectrum : new ArrayList<>();
        this.mzTolerance = mzTolerance;
        this.mzToleranceMode = mzToleranceMode != null ? mzToleranceMode : MzToleranceMode.PPM;
        this.retentionIndex = retentionIndex;
        this.retentionIndexTolerance = retentionIndexTolerance;
        this.derivatizationMethod = derivatizationMethod != null ? derivatizationMethod : derivatizationMethod.METHYL_CHLOROFORMATES;
        this.columnType = columnType != null ? columnType : columnType.SEMISTANDARD_NON_POLAR;
    }

    public List<Spectrum> getGcmsSpectrum() {
        return gcmsSpectrum;
    }

    public void setGcmsSpectrum(List<Spectrum> gcmsSpectrum) {
        this.gcmsSpectrum = gcmsSpectrum != null ? gcmsSpectrum : new ArrayList<>();
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
    //Is PPM or MDA
    public void setMzToleranceMode(MzToleranceMode mzToleranceMode) {
        this.mzToleranceMode = mzToleranceMode != null ? mzToleranceMode : MzToleranceMode.PPM;;
    }

    public double getRetentionIndex() {
        return retentionIndex;
    }

    public void setRetentionIndex(double retentionIndex) {
        this.retentionIndex = retentionIndex;
    }

    public double getRetentionIndexTolerance() {
        return retentionIndexTolerance;
    }

    //MUST BE <0???
    public void setRetentionIndexTolerance(double retentionIndexTolerance) {
        //this.retentionIndexTolerance = retentionIndexTolerance;
        if (retentionIndexTolerance < 0) {
            throw new IllegalArgumentException("retentionIndexTolerance must be non-negative");
        }
        this.retentionIndexTolerance = retentionIndexTolerance;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GCMSSearchRequestDTO that = (GCMSSearchRequestDTO) o;
        return Double.compare(mzTolerance, that.mzTolerance) == 0 &&
               Double.compare(retentionIndex, that.retentionIndex) == 0 &&
               Double.compare(retentionIndexTolerance, that.retentionIndexTolerance) == 0 &&
               Objects.equals(gcmsSpectrum, that.gcmsSpectrum) &&
               mzToleranceMode == that.mzToleranceMode &&
               Objects.equals(derivatizationMethod, that.derivatizationMethod) &&
               Objects.equals(columnType, that.columnType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gcmsSpectrum, mzTolerance, mzToleranceMode, retentionIndex,
                retentionIndexTolerance, derivatizationMethod, columnType);
    }

    @Override
    public String toString() {
        return "GcmsSearchRequestDTO{" +
                "gcmsSpectrum=" + gcmsSpectrum +
                ", mzTolerance=" + mzTolerance +
                ", mzToleranceMode=" + mzToleranceMode +
                ", retentionIndex=" + retentionIndex +
                ", retentionIndexTolerance=" + retentionIndexTolerance +
                ", derivationMethod='" + derivatizationMethod + '\'' +
                ", columnType='" + columnType + '\'' +
                '}';
    }
}
