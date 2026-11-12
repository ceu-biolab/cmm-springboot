package ceu.biolab.cmm.MSMSSearch.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import ceu.biolab.cmm.MSMSSearch.domain.CIDEnergy;
import ceu.biolab.cmm.MSMSSearch.domain.Spectrum;
import ceu.biolab.cmm.MSMSSearch.domain.SpectrumSource;
import ceu.biolab.cmm.shared.domain.ExperimentParameters;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.msFeature.ScoreType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LCMSMSSearchRequestDTO {
    @JsonProperty("CIDEnergy")
    @NotNull
    private CIDEnergy CIDEnergy;

    @Positive
    private Double precursorIonMZ;

    @Positive
    private double tolerancePrecursorIon;

    @NotNull
    private MzToleranceMode toleranceModePrecursorIon;

    @Positive
    private double toleranceFragments;

    @NotNull
    private MzToleranceMode toleranceModeFragments;

    @NotNull
    private IonizationMode ionizationMode;

    @NotNull
    private ScoreType scoreType;

    @NotNull
    private SpectrumSource spectrumSource;

    @Valid
    private Spectrum fragmentsMZsIntensities;

    private Double rtValue;

    private List<@NotNull @Positive Double> precursorIonMZValues = new ArrayList<>();

    @Valid
    private List<@NotNull Spectrum> fragmentsMZsIntensitiesList = new ArrayList<>();

    private List<@NotNull Double> rtValues = new ArrayList<>();

    @Valid
    private ExperimentParameters experimentParameters;

    private List<@NotBlank String> adducts = new ArrayList<>();

    @AssertTrue(message = "Provide either a complete single feature or complete batched features.")
    public boolean hasValidFeatureMode() {
        boolean singleProvided = precursorIonMZ != null || fragmentsMZsIntensities != null || rtValue != null;
        boolean batchProvided = hasValues(precursorIonMZValues) || hasValues(fragmentsMZsIntensitiesList) || hasValues(rtValues);

        if (singleProvided && batchProvided) {
            return false;
        }
        if (!singleProvided && !batchProvided) {
            return false;
        }
        if (singleProvided) {
            return precursorIonMZ != null && fragmentsMZsIntensities != null && rtValue != null;
        }
        return hasValues(precursorIonMZValues)
                && hasValues(fragmentsMZsIntensitiesList)
                && hasValues(rtValues)
                && precursorIonMZValues.size() == fragmentsMZsIntensitiesList.size()
                && precursorIonMZValues.size() == rtValues.size();
    }

    private boolean hasValues(List<?> values) {
        return values != null && !values.isEmpty();
    }
}
