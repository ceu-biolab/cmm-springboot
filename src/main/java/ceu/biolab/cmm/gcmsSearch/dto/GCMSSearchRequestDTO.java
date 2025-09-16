package ceu.biolab.cmm.gcmsSearch.dto;

import ceu.biolab.cmm.gcmsSearch.domain.ColumnType;
import ceu.biolab.cmm.gcmsSearch.domain.DerivatizationMethod;
import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import lombok.Data;

@Data
public class GCMSSearchRequestDTO {

    private Spectrum gcmsSpectrumExperimental;

    private double retentionIndex;
    private double retentionIndexTolerance;

    private DerivatizationMethod derivatizationMethod;
    private ColumnType columnType;

    public GCMSSearchRequestDTO() {
        this(null, 0.0, 0.0, null, null);
    }

    public GCMSSearchRequestDTO(Spectrum gcmsSpectrum, double retentionIndex, double retentionIndexTolerance,
                                DerivatizationMethod derivatizationMethod, ColumnType columnType) {
        this.gcmsSpectrumExperimental = gcmsSpectrum != null ? gcmsSpectrum : new Spectrum();
        this.retentionIndex = retentionIndex;
        setRetentionIndexTolerance(retentionIndexTolerance);
        this.derivatizationMethod = derivatizationMethod != null ? derivatizationMethod : DerivatizationMethod.METHYL_CHLOROFORMATE;
        this.columnType = columnType != null ? columnType : ColumnType.SEMISTANDARD_NON_POLAR;
    }

    public void setRetentionIndexTolerance(double retentionIndexTolerance) {
        if (retentionIndexTolerance < 0) {
            throw new IllegalArgumentException("retentionIndexTolerance must be non-negative");
        }
        this.retentionIndexTolerance = retentionIndexTolerance;
    }

}
