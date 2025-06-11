package ceu.biolab.cmm.MSMS.dto;

import ceu.biolab.cmm.MSMS.domain.CIDEnergy;

import ceu.biolab.cmm.MSMS.domain.Peak;
import ceu.biolab.cmm.MSMS.domain.ToleranceMode;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.msFeature.MSFeature;

import java.util.ArrayList;
import java.util.List;

public class MSMSSearchRequestDTO {
    private double precursorIonMZ;
    private double tolerancePrecursorIon;
    private ToleranceMode toleranceModePrecursorIon; // "PPM" or "mDA"
    private double toleranceFragments;
    private ToleranceMode toleranceModeFragments;     // "PPM" or "mDA"
    private IonizationMode ionizationMode;             // "POSITIVE" or "NEGATIVE"
    private List<String> adducts;              // e.g., ["M+H", "M+Na"]
    private List<Peak> spectrum;               // List of mz-intensity pairs
    private CIDEnergy CIDEnergy;

    public MSMSSearchRequestDTO(double precursorIonMZ, double tolerancePrecursorIon, ToleranceMode toleranceModePrecursorIon, double toleranceFragments, ToleranceMode toleranceModeFragments, IonizationMode ionizationMode, List<String> adducts, List<Peak> spectrum, CIDEnergy CIDEnergy) {
        this.precursorIonMZ = precursorIonMZ;
        this.tolerancePrecursorIon = tolerancePrecursorIon;
        this.toleranceModePrecursorIon = toleranceModePrecursorIon;
        this.toleranceFragments = toleranceFragments;
        this.toleranceModeFragments = toleranceModeFragments;
        this.ionizationMode = ionizationMode;
        this.adducts = adducts;
        this.spectrum = spectrum;
        this.CIDEnergy = CIDEnergy;
    }

    public MSMSSearchRequestDTO() {
        this.precursorIonMZ = 0.0;
        this.tolerancePrecursorIon = 0.0;
        this.toleranceModePrecursorIon= ToleranceMode.mDA;
        this.toleranceFragments = 0.0;
        this.toleranceModeFragments = ToleranceMode.mDA;
        this.ionizationMode=IonizationMode.POSITIVE;
        this.adducts = new ArrayList<>();
        this.spectrum = new ArrayList<>();
    }

    public double getPrecursorIonMZ() {
        return precursorIonMZ;
    }

    public void setPrecursorIonMZ(double precursorIonMZ) {
        this.precursorIonMZ = precursorIonMZ;
    }

    public double getTolerancePrecursorIon() {
        return tolerancePrecursorIon;
    }

    public void setTolerancePrecursorIon(double tolerancePrecursorIon) {
        this.tolerancePrecursorIon = tolerancePrecursorIon;
    }

    public ToleranceMode getToleranceModePrecursorIon() {
        return toleranceModePrecursorIon;
    }

    public void setToleranceModePrecursorIon(ToleranceMode toleranceModePrecursorIon) {
        this.toleranceModePrecursorIon = toleranceModePrecursorIon;
    }

    public double getToleranceFragments() {
        return toleranceFragments;
    }

    public void setToleranceFragments(double toleranceFragments) {
        this.toleranceFragments = toleranceFragments;
    }

    public ToleranceMode getToleranceModeFragments() {
        return toleranceModeFragments;
    }

    public void setToleranceModeFragments(ToleranceMode toleranceModeFragments) {
        this.toleranceModeFragments = toleranceModeFragments;
    }

    public IonizationMode getIonizationMode() {
        return ionizationMode;
    }

    public void setIonizationMode(IonizationMode ionizationMode) {
        this.ionizationMode = ionizationMode;
    }

    public List<String> getAdducts() {
        return adducts;
    }

    public void setAdducts(List<String> adducts) {
        this.adducts = adducts;
    }

    public List<Peak> getSpectrum() {
        return spectrum;
    }

    public void setSpectrum(List<Peak> spectrum) {
        this.spectrum = spectrum;
    }

    public CIDEnergy getCIDEnergy() {
        return CIDEnergy;
    }

    public void setCIDEnergy(CIDEnergy CIDEnergy) {
        this.CIDEnergy = CIDEnergy;
    }
}
