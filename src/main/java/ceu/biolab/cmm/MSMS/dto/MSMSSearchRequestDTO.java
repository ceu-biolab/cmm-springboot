package ceu.biolab.cmm.MSMS.dto;

import ceu.biolab.cmm.MSMS.domain.*;

import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.msFeature.MSFeature;

import java.util.ArrayList;
import java.util.List;

public class MSMSSearchRequestDTO {
    private double precursorIonMZ;
    private double tolerancePrecursorIon;
    private MzToleranceMode toleranceModePrecursorIon; // "PPM" or "mDA"
    private double toleranceFragments;
    private MzToleranceMode toleranceModeFragments;     // "PPM" or "mDA"
    private IonizationMode ionizationMode;             // "POSITIVE" or "NEGATIVE"
    private List<String> adducts;              // e.g., ["M+H", "M+Na"]
    private Spectrum spectrum   ;               // List of mz-intensity pairs
    private CIDEnergy CIDEnergy;
    private ScoreType scoreType;

    public MSMSSearchRequestDTO(double precursorIonMZ, double tolerancePrecursorIon, MzToleranceMode toleranceModePrecursorIon,
                                double toleranceFragments, MzToleranceMode toleranceModeFragments, IonizationMode ionizationMode,
                                List<String> adducts, Spectrum peaks, CIDEnergy CIDEnergy, ScoreType scoreType) {
        this.precursorIonMZ = precursorIonMZ;
        this.tolerancePrecursorIon = tolerancePrecursorIon;
        this.toleranceModePrecursorIon = toleranceModePrecursorIon;
        this.toleranceFragments = toleranceFragments;
        this.toleranceModeFragments = toleranceModeFragments;
        this.ionizationMode = ionizationMode;
        this.adducts = adducts;
        this.spectrum   = peaks;
        this.CIDEnergy = CIDEnergy;
        this.scoreType = scoreType;
    }

    public MSMSSearchRequestDTO() {
        this.precursorIonMZ = 0.0;
        this.tolerancePrecursorIon = 0.0;
        this.toleranceModePrecursorIon= MzToleranceMode.MDA;
        this.toleranceFragments = 0.0;
        this.toleranceModeFragments = MzToleranceMode.MDA;
        this.ionizationMode=IonizationMode.POSITIVE;
        this.adducts = new ArrayList<>();
        this.spectrum = new Spectrum();
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

    public MzToleranceMode getToleranceModePrecursorIon() {
        return toleranceModePrecursorIon;
    }

    public void setToleranceModePrecursorIon(MzToleranceMode toleranceModePrecursorIon) {
        this.toleranceModePrecursorIon = toleranceModePrecursorIon;
    }

    public double getToleranceFragments() {
        return toleranceFragments;
    }

    public void setToleranceFragments(double toleranceFragments) {
        this.toleranceFragments = toleranceFragments;
    }

    public MzToleranceMode getToleranceModeFragments() {
        return toleranceModeFragments;
    }

    public void setToleranceModeFragments(MzToleranceMode toleranceModeFragments) {
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

    public Spectrum getSpectrum() {
        return spectrum;
    }

    public void setSpectrum(Spectrum spectrum) {
        this.spectrum = spectrum;
    }

    public CIDEnergy getCIDEnergy() {
        return CIDEnergy;
    }

    public void setCIDEnergy(CIDEnergy CIDEnergy) {
        this.CIDEnergy = CIDEnergy;
    }

    public ScoreType getScoreType() {
        return scoreType;
    }

    public void setScoreType(ScoreType scoreType) {
        this.scoreType = scoreType;
    }
}
