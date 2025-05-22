package ceu.biolab.cmm.gcms.dto;

import ceu.biolab.cmm.gcms.domain.ColumnType;
import ceu.biolab.cmm.gcms.domain.DerivatizationMethod;
import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class GCMSSearchRequestDTO {

    private List<Spectrum> gcmsSpectrum; //Group of Spectra

    private double retentionIndex;
    private double retentionIndexTolerance;

    private DerivatizationMethod derivatizationMethod;
    private ColumnType columnType;


    public GCMSSearchRequestDTO(List<Spectrum> gcmsSpectrum, double mzTolerance,
                                MzToleranceMode mzToleranceMode, double retentionIndex, double retentionIndexTolerance,
                                DerivatizationMethod derivatizationMethod, ColumnType columnType) {
        this.gcmsSpectrum = gcmsSpectrum != null ? gcmsSpectrum : new ArrayList<>();
        this.retentionIndex = retentionIndex;
        this.retentionIndexTolerance = retentionIndexTolerance;
        this.derivatizationMethod = derivatizationMethod != null ? derivatizationMethod : derivatizationMethod.METHYL_CHLOROFORMATES;
        this.columnType = columnType != null ? columnType : columnType.SEMISTANDARD_NON_POLAR;
    }


    public void setGcmsSpectrum(List<Spectrum> gcmsSpectrum) {
        this.gcmsSpectrum = gcmsSpectrum != null ? gcmsSpectrum : new ArrayList<>();
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
        return Double.compare(retentionIndex, that.retentionIndex) == 0 &&
               Double.compare(retentionIndexTolerance, that.retentionIndexTolerance) == 0 &&
               Objects.equals(gcmsSpectrum, that.gcmsSpectrum) &&
               Objects.equals(derivatizationMethod, that.derivatizationMethod) &&
               Objects.equals(columnType, that.columnType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gcmsSpectrum, retentionIndex,
                retentionIndexTolerance, derivatizationMethod, columnType);
    }

    @Override
    public String toString() {
        return "GcmsSearchRequestDTO{" +
                "gcmsSpectrum=" + gcmsSpectrum +
                ", retentionIndex=" + retentionIndex +
                ", retentionIndexTolerance=" + retentionIndexTolerance +
                ", derivationMethod='" + derivatizationMethod + '\'' +
                ", columnType='" + columnType + '\'' +
                '}';
    }
}
