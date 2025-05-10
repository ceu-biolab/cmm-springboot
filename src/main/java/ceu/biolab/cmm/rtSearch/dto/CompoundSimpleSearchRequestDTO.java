package ceu.biolab.cmm.rtSearch.dto;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import ceu.biolab.cmm.shared.domain.*;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import com.github.jsonldjava.utils.Obj;
import org.xmlcml.euclid.Int;


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
        }else {
            this.tolerance = tolerance;
        }
        this.ionizationMode = ionizationMode;
        this.adductsString = adductsString;
        this.detectedAdduct = detectedAdduct;
        this.formulaTypeInt = formulaTypeInt;
        this.databases = databases;
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
        this.adductsString = adductsString;
    }

    public Optional<String> getDetectedAdduct() {
        return detectedAdduct;
    }

    public void setDetectedAdduct(Optional<String> detectedAdduct) {
        this.detectedAdduct = detectedAdduct;
    }

    public Optional<Integer> getFormulaTypeInt() {
        return formulaTypeInt;
    }

    public void setFormulaTypeInt(Optional<Integer> formulaTypeInt) {
        this.formulaTypeInt = formulaTypeInt;
    }

    public Set<Database> getDatabases() {
        return databases;
    }

    public void setDatabases(Set<Database> databases) {
        this.databases = databases;
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
                && ionizationMode == that.ionizationMode && Objects.equals(detectedAdduct, that.detectedAdduct) && Objects.equals(formulaTypeInt, that.formulaTypeInt)
                && Objects.equals(adductsString, that.adductsString) && Objects.equals(databases, that.databases) && metaboliteType == that.metaboliteType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mz, mzToleranceMode, tolerance, ionizationMode, detectedAdduct, formulaTypeInt, adductsString, databases, metaboliteType);
    }

    @Override
    public String toString() {
        return "CompoundSimpleSearchRequestDTO{" +
                "mz=" + mz +
                ", mzToleranceMode=" + mzToleranceMode +
                ", tolerance=" + tolerance +
                ", ionizationMode=" + ionizationMode +
                ", detectedAdduct=" + detectedAdduct +
                ", formulaTypeInt=" + formulaTypeInt +
                ", adductsString=" + adductsString +
                ", databases=" + databases +
                ", metaboliteType=" + metaboliteType +
                '}';
    }
}



