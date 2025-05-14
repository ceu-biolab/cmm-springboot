package ceu.biolab.cmm.rtSearch.dto;

import java.util.Optional;
import java.util.Set;

import ceu.biolab.cmm.shared.domain.*;
import ceu.biolab.cmm.shared.domain.IonizationMode;

import lombok.Data;

@Data
public class CompoundSimpleSearchRequestDTO {
    private Double mz;
    private MzToleranceMode mzToleranceMode;
    private Double tolerance;
    private IonizationMode ionizationMode;
    private Optional<String> detectedAdduct;
    private Optional<Integer> formulaTypeInt;
    private Set<String> adductsString;
    private Set<Database> databases;
    private MetaboliteType metaboliteType;

    public CompoundSimpleSearchRequestDTO(Double mz, MzToleranceMode mzToleranceMode, Double tolerance, IonizationMode ionizationMode,
                                          Set<String> adductsString, Optional<String> detectedAdduct, Optional<Integer> formulaTypeInt, Set<Database> databases, MetaboliteType metaboliteType) {
        this.mz = mz;
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



