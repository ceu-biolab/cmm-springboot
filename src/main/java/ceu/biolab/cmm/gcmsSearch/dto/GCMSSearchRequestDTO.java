package ceu.biolab.cmm.gcmsSearch.dto;

import ceu.biolab.cmm.gcmsSearch.domain.ColumnType;
import ceu.biolab.cmm.gcmsSearch.domain.DerivatizationMethod;
import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class GCMSSearchRequestDTO {

    @NotNull
    @Valid
    private Spectrum gcmsSpectrumExperimental;

    @Positive
    private double retentionIndex;
    @Positive
    private double retentionIndexTolerance;

    @NotNull
    private DerivatizationMethod derivatizationMethod;
    @NotNull
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
        this.retentionIndexTolerance = retentionIndexTolerance;
    }

}
