package ceu.biolab.cmm.rtSearch.dto;

import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.Data;

@Data
public class CompoundBatchSearchRequestDTO {
    private List<Double> mzValues;
    private MzToleranceMode mzToleranceMode;
    private Double tolerance;
    private IonizationMode ionizationMode;
    private Optional<String> detectedAdduct;
    private Optional<Integer> formulaTypeInt;
    private Set<String> adductsString;
    private Set<Database> databases;
    private MetaboliteType metaboliteType;

    public CompoundBatchSearchRequestDTO(List<Double> mzValues, MzToleranceMode mzToleranceMode, Double tolerance, IonizationMode ionizationMode,
                                         Set<String> adductsString, Optional<String> detectedAdduct, Optional<Integer> formulaTypeInt, Set<Database> databases, MetaboliteType metaboliteType) {
        this.mzValues = mzValues;
        this.mzToleranceMode = mzToleranceMode;
        if (tolerance < 0) {
            throw new IllegalArgumentException("mzTolerance must be non-negative");
        } else {
            this.tolerance = tolerance;
        }
        this.ionizationMode = ionizationMode;
        this.adductsString = adductsString;
        this.detectedAdduct = detectedAdduct;
        this.formulaTypeInt = formulaTypeInt;
        this.databases = databases;
        this.metaboliteType = metaboliteType;
    }
}

