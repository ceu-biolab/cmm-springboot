package ceu.biolab.cmm.msSearch.dto;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public class CompoundSimpleSearchRequestDTO {
    @NotNull
    @Positive
    private Double mz;
    @NotNull
    private MzToleranceMode mzToleranceMode;
    @NotNull
    @Positive
    private Double tolerance;
    @NotNull
    private IonizationMode ionizationMode;
    @NotNull
    private Optional<String> detectedAdduct;
    @NotNull
    private Optional<FormulaType> formulaType;
    @NotEmpty
    private Set<@NotBlank String> adductsString;
    @NotEmpty
    private Set<@NotNull Database> databases;
    @NotNull
    private MetaboliteType metaboliteType;

    public CompoundSimpleSearchRequestDTO() {
        this.mz = 0.0;
        this.mzToleranceMode = MzToleranceMode.PPM;
        this.tolerance = 0.0;
        this.ionizationMode = IonizationMode.POSITIVE;
        this.detectedAdduct = Optional.empty();
        this.formulaType = Optional.empty();
        this.adductsString = new LinkedHashSet<>();
        this.databases = new LinkedHashSet<>();
        this.metaboliteType = MetaboliteType.ALL;
    }

    public CompoundSimpleSearchRequestDTO(Double mz, MzToleranceMode mzToleranceMode, Double tolerance, IonizationMode ionizationMode,
                                          Set<String> adductsString, Optional<String> detectedAdduct, Optional<FormulaType> formulaType, Set<Database> databases, MetaboliteType metaboliteType) {
        this.mz = mz;
        this.mzToleranceMode = mzToleranceMode;
        if (tolerance <= 0) {
            throw new IllegalArgumentException("mzTolerance must be greater than zero");
        }else {
            this.tolerance = tolerance;
        }
        this.ionizationMode = ionizationMode;
        this.adductsString = adductsString == null ? new LinkedHashSet<>() : adductsString;
        this.detectedAdduct = detectedAdduct == null ? Optional.empty() : detectedAdduct;
        this.formulaType = formulaType == null ? Optional.empty() : formulaType;
        this.databases = databases == null ? new LinkedHashSet<>() : databases;
        this.metaboliteType = metaboliteType;
    }

    public Double getMz() {
        return mz;
    }

    public void setMz(Double mz) {
        this.mz = mz;
    }

    public MzToleranceMode getMzToleranceMode() {
        return mzToleranceMode;
    }

    public void setMzToleranceMode(MzToleranceMode mzToleranceMode) {
        this.mzToleranceMode = mzToleranceMode;
    }

    public Double getTolerance() {
        return tolerance;
    }

    public void setTolerance(Double tolerance) {
        this.tolerance = tolerance;
    }

    public IonizationMode getIonizationMode() {
        return ionizationMode;
    }

    public void setIonizationMode(IonizationMode ionizationMode) {
        this.ionizationMode = ionizationMode;
    }

    public Set<String> getAdductsString() {
        return adductsString;
    }

    public void setAdductsString(Set<String> adductsString) {
        this.adductsString = adductsString == null ? new LinkedHashSet<>() : adductsString;
    }

    public Optional<String> getDetectedAdduct() {
        return detectedAdduct;
    }

    public void setDetectedAdduct(Optional<String> detectedAdduct) {
        this.detectedAdduct = detectedAdduct == null ? Optional.empty() : detectedAdduct;
    }

    public Optional<FormulaType> getFormulaType() {
        return formulaType;
    }

    public void setFormulaType(Optional<FormulaType> formulaType) {
        this.formulaType = formulaType == null ? Optional.empty() : formulaType;
    }

    public Set<Database> getDatabases() {
        return databases;
    }

    public void setDatabases(Set<Database> databases) {
        this.databases = databases == null ? new LinkedHashSet<>() : databases;
    }

    public MetaboliteType getMetaboliteType() {
        return metaboliteType;
    }

    public void setMetaboliteType(MetaboliteType metaboliteType) {
        this.metaboliteType = metaboliteType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompoundSimpleSearchRequestDTO that = (CompoundSimpleSearchRequestDTO) o;
        return Objects.equals(mz, that.mz) && mzToleranceMode == that.mzToleranceMode && Objects.equals(tolerance, that.tolerance)
                && ionizationMode == that.ionizationMode && Objects.equals(detectedAdduct, that.detectedAdduct) && Objects.equals(formulaType, that.formulaType)
                && Objects.equals(adductsString, that.adductsString) && Objects.equals(databases, that.databases) && metaboliteType == that.metaboliteType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mz, mzToleranceMode, tolerance, ionizationMode, detectedAdduct, formulaType, adductsString, databases, metaboliteType);
    }

    @Override
    public String toString() {
        return "CompoundSimpleSearchRequestDTO{" +
                "mz=" + mz +
                ", mzToleranceMode=" + mzToleranceMode +
                ", tolerance=" + tolerance +
                ", ionizationMode=" + ionizationMode +
                ", detectedAdduct=" + detectedAdduct +
                ", formulaType=" + formulaType +
                ", adductsString=" + adductsString +
                ", databases=" + databases +
                ", metaboliteType=" + metaboliteType +
                '}';
    }
}
