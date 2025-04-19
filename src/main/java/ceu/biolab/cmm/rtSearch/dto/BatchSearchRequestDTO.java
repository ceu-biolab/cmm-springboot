package ceu.biolab.cmm.rtSearch.dto;

import ceu.biolab.cmm.ccsSearch.domain.BufferGas;
import ceu.biolab.cmm.ccsSearch.domain.CcsToleranceMode;
import ceu.biolab.cmm.ccsSearch.dto.CcsSearchRequest;
import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class BatchSearchRequestDTO {

    private List<Double> mzValues;
    private MzToleranceMode mzToleranceMode;
    private Double tolerance;
    private IonizationMode ionizationMode;
    private Set<String> adductsString;
    private Set<Database> databases;

    private MetaboliteType metaboliteType;

    public BatchSearchRequestDTO(List<Double> mzValues, MzToleranceMode mzToleranceMode, Double tolerance, IonizationMode ionizationMode,
                                 Set<String> adductsString, Set<Database> databases, MetaboliteType metaboliteType) {
        this.mzValues = mzValues;
        this.mzToleranceMode = mzToleranceMode;
        this.tolerance = tolerance;
        this.ionizationMode = ionizationMode;
        this.adductsString = adductsString;
        this.databases = databases;
        this.metaboliteType = metaboliteType;
    }

    public List<Double> getMzValues() {
        return mzValues;
    }

    public void setMzValues(List<Double> mzValues) {
        this.mzValues = mzValues;
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
        BatchSearchRequestDTO that = (BatchSearchRequestDTO) o;
        return Objects.equals(mzValues, that.mzValues) && mzToleranceMode == that.mzToleranceMode && Objects.equals(tolerance, that.tolerance) && ionizationMode == that.ionizationMode && Objects.equals(adductsString, that.adductsString) && Objects.equals(databases, that.databases) && metaboliteType == that.metaboliteType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mzValues, mzToleranceMode, tolerance, ionizationMode, adductsString, databases, metaboliteType);
    }


    @Override
    public String toString() {
        return "BatchSearchRequestDTO{" +
                "mzValues=" + mzValues +
                ", mzToleranceMode=" + mzToleranceMode +
                ", tolerance=" + tolerance +
                ", ionizationMode=" + ionizationMode +
                ", adductsString=" + adductsString +
                ", databases=" + databases +
                ", metaboliteType=" + metaboliteType +
                '}';
    }
}

