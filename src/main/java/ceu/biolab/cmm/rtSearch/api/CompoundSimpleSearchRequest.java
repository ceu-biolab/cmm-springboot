package ceu.biolab.cmm.rtSearch.api;

import ceu.biolab.cmm.rtSearch.model.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Set;

public class CompoundSimpleSearchRequest {
    private Double mz;
    private ToleranceMode toleranceMode;
    private Double tolerance;
    private IonizationMode ionizationMode;
    private Set<String> adductsString;
    private Set<Databases> databases;
    private MetaboliteType metaboliteType;

    @JsonCreator
    public CompoundSimpleSearchRequest(
            @JsonProperty("mz") Double mz,
            @JsonProperty("toleranceMode") String toleranceMode,
            @JsonProperty("tolerance") Double tolerance,
            @JsonProperty("ionizationMode") String ionizationMode,
            @JsonProperty("adductsString") Set<String> adductsString,
            @JsonProperty("databases") Set<String> databases,
            @JsonProperty("metaboliteType") String metaboliteType) {

        this.mz = mz;
        this.toleranceMode = ParserJSON.parseToleranceMode(toleranceMode);
        this.tolerance = tolerance;
        this.ionizationMode = ParserJSON.parseIonizationMode(ionizationMode);
        this.adductsString = adductsString;

        this.databases = new HashSet<>();
        for (String db : databases) {
            this.databases.add(ParserJSON.parseDatabases(db));
        }

        this.metaboliteType = ParserJSON.parseMetaboliteType(metaboliteType);
    }


    // Getters and Setters
    public Double getMz() { return mz; }
    public void setMz(Double mz) { this.mz = mz; }

    public ToleranceMode getToleranceMode() { return toleranceMode; }
    public void setToleranceMode(ToleranceMode toleranceMode) { this.toleranceMode = toleranceMode; }

    public Double getTolerance() { return tolerance; }
    public void setTolerance(Double tolerance) { this.tolerance = tolerance; }

    public IonizationMode getIonizationMode() { return ionizationMode; }
    public void setIonizationMode(IonizationMode ionizationMode) { this.ionizationMode = ionizationMode; }

    public Set<String> getAdductsString() { return adductsString; }
    public void setAdductsString(Set<String> adductsString) { this.adductsString = adductsString; }

    public Set<Databases> getDatabases() { return databases; }
    public void setDatabases(Set<Databases> databases) { this.databases = databases; }

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

