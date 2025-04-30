package ceu.biolab.cmm.batchAdvancedSearch.dto;

import ceu.biolab.cmm.batchAdvancedSearch.domain.ChemicalAlphabet;
import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class BatchAdvancedSearchRequestDTO {
    private Double mz;
    private MzToleranceMode mzToleranceMode;
    private Double tolerance;
    private IonizationMode ionizationMode;
    private Optional<String> detectedAdduct;
    private Set<String> adductsString;
    private Set<Database> databases;
    private MetaboliteType metaboliteType;
    private double retentionTime;
    private Map<Double, Double> compositeSpectrum;
    private ChemicalAlphabet chemicalAlphabet;
    private boolean deuterium;
    private String modifiersType;

    public BatchAdvancedSearchRequestDTO(Double mz, MzToleranceMode mzToleranceMode, Double tolerance,
                                         IonizationMode ionizationMode, Optional<String> detectedAdduct, Set<String> adductsString,
                                         Set<Database> databases, MetaboliteType metaboliteType, double retentionTime,
                                         Map<Double, Double> compositeSpectrum, ChemicalAlphabet chemicalAlphabet, boolean deuterium, String modifiersType) {
        this.mz = mz;
        this.mzToleranceMode = mzToleranceMode;
        this.tolerance = tolerance;
        this.ionizationMode = ionizationMode;
        this.detectedAdduct = detectedAdduct;
        this.adductsString = adductsString;
        this.databases = databases;
        this.metaboliteType = metaboliteType;
        this.retentionTime = retentionTime;
        this.compositeSpectrum = compositeSpectrum;
        this.chemicalAlphabet = chemicalAlphabet;
        this.deuterium = deuterium;
        this.modifiersType = modifiersType;
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

    public Optional<String> getDetectedAdduct() {
        return detectedAdduct;
    }

    public void setDetectedAdduct(Optional<String> detectedAdduct) {
        this.detectedAdduct = detectedAdduct;
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

    public double getRetentionTime() {
        return retentionTime;
    }

    public void setRetentionTime(double retentionTime) {
        this.retentionTime = retentionTime;
    }

    public Map<Double, Double> getCompositeSpectrum() {
        return compositeSpectrum;
    }

    public void setCompositeSpectrum(Map<Double, Double> compositeSpectrum) {
        this.compositeSpectrum = compositeSpectrum;
    }

    public ChemicalAlphabet getchemicalAlphabet() {
        return chemicalAlphabet;
    }

    public void setchemicalAlphabet(ChemicalAlphabet chemicalAlphabet) {
        this.chemicalAlphabet = chemicalAlphabet;
    }

    public boolean isDeuterium() {
        return deuterium;
    }

    public void setDeuterium(boolean deuterium) {
        this.deuterium = deuterium;
    }

    public String getModifiersType() {
        return modifiersType;
    }

    public void setModifiersType(String modifiersType) {
        this.modifiersType = modifiersType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BatchAdvancedSearchRequestDTO that = (BatchAdvancedSearchRequestDTO) o;
        return Double.compare(that.retentionTime, retentionTime) == 0 && deuterium == that.deuterium && Objects.equals(mz, that.mz) && mzToleranceMode == that.mzToleranceMode && Objects.equals(tolerance, that.tolerance) && ionizationMode == that.ionizationMode && Objects.equals(detectedAdduct, that.detectedAdduct) && Objects.equals(adductsString, that.adductsString) && Objects.equals(databases, that.databases) && metaboliteType == that.metaboliteType && Objects.equals(compositeSpectrum, that.compositeSpectrum) && chemicalAlphabet == that.chemicalAlphabet && Objects.equals(modifiersType, that.modifiersType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mz, mzToleranceMode, tolerance, ionizationMode, detectedAdduct, adductsString, databases, metaboliteType, retentionTime, compositeSpectrum, chemicalAlphabet, deuterium, modifiersType);
    }

    @Override
    public String toString() {
        return "BatchAdvancedSearchRequestDTO{" +
                "mz=" + mz +
                ", mzToleranceMode=" + mzToleranceMode +
                ", tolerance=" + tolerance +
                ", ionizationMode=" + ionizationMode +
                ", detectedAdduct=" + detectedAdduct +
                ", adductsString=" + adductsString +
                ", databases=" + databases +
                ", metaboliteType=" + metaboliteType +
                ", retentionTime=" + retentionTime +
                ", compositeSpectrum=" + compositeSpectrum +
                ", chemicalAlphabet=" + chemicalAlphabet +
                ", deuterium=" + deuterium +
                ", modifiersType='" + modifiersType + '\'' +
                '}';
    }
}

