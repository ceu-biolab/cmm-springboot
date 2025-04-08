package ceu.biolab.cmm.rtSearch.api;

import ceu.biolab.cmm.rtSearch.model.*;

import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ceu.biolab.cmm.rtSearch.service.CompoundService;
import java.util.List;


public class CompoundBatchSearchRequest {
    private Double[] mz;
    private ToleranceMode toleranceMode;
    private Double tolerance;
    private IonizationMode ionizationMode;
    private Set<String> adductsString;
    private Set<Database> databases;

    private MetaboliteType metaboliteType;

    public CompoundBatchSearchRequest(
            Double[] mz,
            String toleranceMode,
            Double tolerance,
            String ionizationMode,
            Set<String> adductsString,
            Set<Database> databases,
            MetaboliteType metaboliteType) {

        this.mz = mz;
        this.toleranceMode = ParserJSON.parseToleranceMode(toleranceMode);
        this.tolerance = tolerance;
        this.ionizationMode = ParserJSON.parseIonizationMode(ionizationMode);
        this.adductsString = adductsString;

        this.databases = databases;
        /*for (String db : databases) {
            this.databases.add(ParserJSON.parseDatabases(db));
        }
         */

        //this.metaboliteType = ParserJSON.parseMetaboliteType(metaboliteType);
        this.metaboliteType = metaboliteType;
    }


    // Getters and Setters
    public Double[] getMz() {
        return mz;
    }

    public void setMz(Double[] mz) {
        this.mz = mz;
    }

    public ToleranceMode getToleranceMode() {
        return toleranceMode;
    }

    public void setToleranceMode(ToleranceMode toleranceMode) {
        this.toleranceMode = toleranceMode;
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
    public String toString() {
        return "CompoundSearchRequest{" +
                "mz=" + mz +
                ", toleranceMode=" + toleranceMode +
                ", tolerance=" + tolerance +
                ", ionizationMode=" + ionizationMode +
                ", adductsString=" + adductsString +
                ", databases=" + databases +
                ", metaboliteType=" + metaboliteType +
                '}';
    }
}