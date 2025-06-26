package ceu.biolab.cmm.gcmsSearch.dto;

import ceu.biolab.cmm.gcmsSearch.domain.ColumnType;
import ceu.biolab.cmm.gcmsSearch.domain.DerivatizationMethod;
import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
//THIS IS WHAT THE USER IS USING TO FIND THE COMPOUNDS
public class GCMSSearchRequestDTO {

    //necesito aaqui el spectro? -> no
    //private List<Spectrum> gcmsSpectrum; //Group of Spectra

    private double retentionIndex;
    private double retentionIndexTolerance;

    private DerivatizationMethod derivatizationMethod;
    private ColumnType columnType;


    public GCMSSearchRequestDTO(List<Spectrum> gcmsSpectrum, double retentionIndex, double retentionIndexTolerance,
                                DerivatizationMethod derivatizationMethod, ColumnType columnType) {
        //this.gcmsSpectrum = gcmsSpectrum != null ? gcmsSpectrum : new ArrayList<>();
        this.retentionIndex = retentionIndex;
        this.retentionIndexTolerance = retentionIndexTolerance;
        this.derivatizationMethod = derivatizationMethod != null ? derivatizationMethod : derivatizationMethod.METHYL_CHLOROFORMATE;
        this.columnType = columnType != null ? columnType : columnType.SEMISTANDARD_NON_POLAR;
    }


    public void setGcmsSpectrum(List<Spectrum> gcmsSpectrum) {
        //this.gcmsSpectrum = gcmsSpectrum != null ? gcmsSpectrum : new ArrayList<>();
    }

    //MUST BE >0
    public void setRetentionIndexTolerance(double retentionIndexTolerance) {
        //this.retentionIndexTolerance = retentionIndexTolerance;
        if (retentionIndexTolerance < 0) {
            throw new IllegalArgumentException("retentionIndexTolerance must be non-negative");
        }
        this.retentionIndexTolerance = retentionIndexTolerance;
    }

}
