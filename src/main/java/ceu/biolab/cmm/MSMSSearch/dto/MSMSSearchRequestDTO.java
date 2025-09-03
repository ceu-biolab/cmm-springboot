package ceu.biolab.cmm.MSMSSearch.dto;

import ceu.biolab.cmm.MSMSSearch.domain.*;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MSMSSearchRequestDTO {
    private CIDEnergy CIDEnergy;
    private double precursorIonMZ;
    private double tolerancePrecursorIon;
    private MzToleranceMode toleranceModePrecursorIon; // "PPM" or "mDA"
    private double toleranceFragments;
    private MzToleranceMode toleranceModeFragments;     // "PPM" or "mDA"
    private IonizationMode ionizationMode;             // "POSITIVE" or "NEGATIVE"
    private List<String> adducts;              // e.g., ["M+H", "M+Na"]
    private Spectrum fragmentsMZsIntensities;   // List of mz-intensity pairs
    private ScoreType scoreType;

    public MSMSSearchRequestDTO(CIDEnergy CIDEnergy, double precursorIonMZ, double tolerancePrecursorIon, MzToleranceMode toleranceModePrecursorIon,
                                double toleranceFragments, MzToleranceMode toleranceModeFragments, IonizationMode ionizationMode,
                                List<String> adducts, Spectrum fragmentsMZsIntensities, ScoreType scoreType) {
        this.CIDEnergy = CIDEnergy;
        this.precursorIonMZ = precursorIonMZ;
        this.tolerancePrecursorIon = tolerancePrecursorIon;
        this.toleranceModePrecursorIon = toleranceModePrecursorIon;
        this.toleranceFragments = toleranceFragments;
        this.toleranceModeFragments = toleranceModeFragments;
        this.ionizationMode = ionizationMode;
        this.adducts = adducts;
        this.fragmentsMZsIntensities = fragmentsMZsIntensities;
        this.scoreType = scoreType;
    }

    public MSMSSearchRequestDTO() {
        this.CIDEnergy = ceu.biolab.cmm.MSMSSearch.domain.CIDEnergy.MED;
        this.precursorIonMZ = 0.0;
        this.tolerancePrecursorIon = 0.0;
        this.toleranceModePrecursorIon= MzToleranceMode.MDA;
        this.toleranceFragments = 0.0;
        this.toleranceModeFragments = MzToleranceMode.MDA;
        this.ionizationMode=IonizationMode.POSITIVE;
        this.adducts = new ArrayList<>();
        this.fragmentsMZsIntensities = new Spectrum();
        this.scoreType = ScoreType.COSINE;
    }

}
