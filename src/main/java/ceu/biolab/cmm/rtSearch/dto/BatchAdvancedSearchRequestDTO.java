package ceu.biolab.cmm.rtSearch.dto;

import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;

import java.util.*;

public class BatchAdvancedSearchRequestDTO {
    private List<Double> mzs;
    private MzToleranceMode mzToleranceMode;
    private Double tolerance;
    private IonizationMode ionizationMode;
    private Optional<String> detectedAdduct;
    private Set<String> adductsString;
    private Set<Database> databases;
    private MetaboliteType metaboliteType;
    private List<Double> retentionTimes;
    private List<Map<Double, Double>> compositeSpectrum;
    private FormulaType formulaType;
    private boolean deuterium;
    private String modifiersType;

    public BatchAdvancedSearchRequestDTO(List<Double> mzs, MzToleranceMode mzToleranceMode, Double tolerance,
                                         IonizationMode ionizationMode, Optional<String> detectedAdduct, Set<String> adductsString,
                                         Set<Database> databases, MetaboliteType metaboliteType, List<Double> retentionTimes,
                                         List<Map<Double, Double>> compositeSpectrum, FormulaType formulaType, boolean deuterium, String modifiersType) {
        this.mzs = mzs;
        this.mzToleranceMode = mzToleranceMode;
        this.tolerance = tolerance;
        this.ionizationMode = ionizationMode;
        this.detectedAdduct = detectedAdduct;
        this.adductsString = adductsString;
        this.databases = databases;
        this.metaboliteType = metaboliteType;
        this.retentionTimes = retentionTimes;
        this.compositeSpectrum = compositeSpectrum;
        this.formulaType = formulaType;
        this.deuterium = deuterium;
        this.modifiersType = modifiersType;
    }

    public List<Double> getMz() {
        return mzs;
    }

    public void setMz(List<Double> mzs) {
        this.mzs = mzs;
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

    public FormulaType getFormulaType() {
        return formulaType;
    }

    public void setFormulaType(FormulaType formulaType) {
        this.formulaType = formulaType;
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

    public List<Double> getRetentionTimes() {
        return retentionTimes;
    }

    public void setRetentionTimes(List<Double> retentionTimes) {
        this.retentionTimes = retentionTimes;
    }

    public List<Map<Double, Double>> getCompositeSpectrum() {
        return compositeSpectrum;
    }

    public void setCompositeSpectrum(List<Map<Double, Double>> compositeSpectrum) {
        this.compositeSpectrum = compositeSpectrum;
    }

    public FormulaType getchemicalAlphabet() {
        return formulaType;
    }

    public void setchemicalAlphabet(FormulaType formulaType) {
        this.formulaType = formulaType;
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
        return deuterium == that.deuterium && Objects.equals(mzs, that.mzs) && mzToleranceMode == that.mzToleranceMode && Objects.equals(tolerance, that.tolerance) && ionizationMode == that.ionizationMode && Objects.equals(detectedAdduct, that.detectedAdduct) && Objects.equals(adductsString, that.adductsString) && Objects.equals(databases, that.databases) && metaboliteType == that.metaboliteType && Objects.equals(retentionTimes, that.retentionTimes) && Objects.equals(compositeSpectrum, that.compositeSpectrum) && formulaType == that.formulaType && Objects.equals(modifiersType, that.modifiersType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mzs, mzToleranceMode, tolerance, ionizationMode, detectedAdduct, adductsString, databases, metaboliteType, retentionTimes, compositeSpectrum, formulaType, deuterium, modifiersType);
    }

    @Override
    public String toString() {
        return "BatchAdvancedSearchRequestDTO{" +
                "mzs=" + mzs +
                ", mzToleranceMode=" + mzToleranceMode +
                ", tolerance=" + tolerance +
                ", ionizationMode=" + ionizationMode +
                ", detectedAdduct=" + detectedAdduct +
                ", adductsString=" + adductsString +
                ", databases=" + databases +
                ", metaboliteType=" + metaboliteType +
                ", retentionTimes=" + retentionTimes +
                ", compositeSpectrum=" + compositeSpectrum +
                ", formulaType=" + formulaType +
                ", deuterium=" + deuterium +
                ", modifiersType='" + modifiersType + '\'' +
                '}';
    }
}
