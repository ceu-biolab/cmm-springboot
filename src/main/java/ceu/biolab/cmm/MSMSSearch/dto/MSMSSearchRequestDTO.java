package ceu.biolab.cmm.MSMSSearch.dto;

import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.msFeature.ScoreType;
import ceu.biolab.cmm.MSMSSearch.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MSMSSearchRequestDTO {
    @NotNull
    private CIDEnergy CIDEnergy;
    @Positive
    private double precursorIonMZ;
    @Positive
    private double tolerancePrecursorIon;
    @NotNull
    private MzToleranceMode toleranceModePrecursorIon; // "PPM" or "mDA"
    @Positive
    private double toleranceFragments;
    @NotNull
    private MzToleranceMode toleranceModeFragments;     // "PPM" or "mDA"
    @NotNull
    private IonizationMode ionizationMode;             // "POSITIVE" or "NEGATIVE"
    @NotEmpty
    private List<@NotBlank String> adducts;              // e.g., ["[M+H]+", "[M+Na]+"]
    @NotNull
    @Valid
    private Spectrum fragmentsMZsIntensities;   // List of mz-intensity pairs
    @NotNull
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
        this.scoreType = null;
    }

}
