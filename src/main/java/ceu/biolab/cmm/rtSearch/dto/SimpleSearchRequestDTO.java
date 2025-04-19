package ceu.biolab.cmm.rtSearch.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ceu.biolab.cmm.ccsSearch.domain.BufferGas;
import ceu.biolab.cmm.ccsSearch.domain.CcsToleranceMode;
import ceu.biolab.cmm.shared.domain.*;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;



public class SimpleSearchRequestDTO {
    private Double mz;
    private MzToleranceMode mzToleranceMode;
    private Double tolerance;
    private IonizationMode ionizationMode;
    private Set<String> adductsString;
    private Set<Database> databases;
    private MetaboliteType metaboliteType;

    public SimpleSearchRequestDTO(Double mz, MzToleranceMode mzToleranceMode, Double tolerance, IonizationMode ionizationMode,
                                  Set<String> adductsString, Set<Database> databases, MetaboliteType metaboliteType) {
        this.mz = mz;
        this.mzToleranceMode = mzToleranceMode;
        this.tolerance = tolerance;
        this.ionizationMode = ionizationMode;
        this.adductsString = adductsString;
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
        SimpleSearchRequestDTO that = (SimpleSearchRequestDTO) o;
        return Objects.equals(mz, that.mz) && mzToleranceMode == that.mzToleranceMode && Objects.equals(tolerance, that.tolerance) && ionizationMode == that.ionizationMode && Objects.equals(adductsString, that.adductsString) && Objects.equals(databases, that.databases) && metaboliteType == that.metaboliteType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mz, mzToleranceMode, tolerance, ionizationMode, adductsString, databases, metaboliteType);
    }


    @Override
    public String toString() {
        return "SimpleSearchRequestDTO{" +
                "mz=" + mz +
                ", mzToleranceMode=" + mzToleranceMode +
                ", tolerance=" + tolerance +
                ", ionizationMode=" + ionizationMode +
                ", adductsString=" + adductsString +
                ", databases=" + databases +
                ", metaboliteType=" + metaboliteType +
                '}';
    }
}

