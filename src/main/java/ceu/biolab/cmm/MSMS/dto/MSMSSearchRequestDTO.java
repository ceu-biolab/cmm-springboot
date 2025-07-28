package ceu.biolab.cmm.MSMS.dto;

import ceu.biolab.cmm.MSMS.domain.*;

import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.msFeature.MSFeature;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
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

}
