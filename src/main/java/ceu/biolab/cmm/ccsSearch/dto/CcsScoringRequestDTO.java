package ceu.biolab.cmm.ccsSearch.dto;

import ceu.biolab.cmm.ccsSearch.domain.BufferGas;
import ceu.biolab.cmm.ccsSearch.domain.CcsToleranceMode;
import ceu.biolab.cmm.shared.domain.ExperimentParameters;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CcsScoringRequestDTO extends CcsSearchRequestDTO {
    @NotEmpty
    private List<@NotNull Double> rtValues = new ArrayList<>();

    @Valid
    private ExperimentParameters experimentParameters;

    private List<@NotEmpty Map<@NotNull @Positive Double, @NotNull @PositiveOrZero Double>> compositeSpectrum = new ArrayList<>();

    public CcsScoringRequestDTO() {
        super();
    }

    public CcsScoringRequestDTO(List<Double> mzValues,
                                double mzTolerance,
                                MzToleranceMode mzToleranceMode,
                                List<Double> ccsValues,
                                double ccsTolerance,
                                CcsToleranceMode ccsToleranceMode,
                                IonizationMode ionizationMode,
                                BufferGas bufferGas,
                                List<String> adducts,
                                List<Double> rtValues,
                                ExperimentParameters experimentParameters,
                                List<Map<Double, Double>> compositeSpectrum) {
        super(mzValues, mzTolerance, mzToleranceMode, ccsValues, ccsTolerance, ccsToleranceMode, ionizationMode, bufferGas, adducts);
        this.rtValues = rtValues != null ? new ArrayList<>(rtValues) : new ArrayList<>();
        this.experimentParameters = experimentParameters;
        this.compositeSpectrum = compositeSpectrum != null ? new ArrayList<>(compositeSpectrum) : new ArrayList<>();
    }

    public CcsScoringRequestDTO(List<Double> mzValues,
                                double mzTolerance,
                                MzToleranceMode mzToleranceMode,
                                List<Double> ccsValues,
                                double ccsTolerance,
                                CcsToleranceMode ccsToleranceMode,
                                IonizationMode ionizationMode,
                                BufferGas bufferGas,
                                List<String> adducts,
                                List<Double> rtValues,
                                ExperimentParameters experimentParameters) {
        this(mzValues, mzTolerance, mzToleranceMode, ccsValues, ccsTolerance, ccsToleranceMode,
                ionizationMode, bufferGas, adducts, rtValues, experimentParameters, null);
    }

    public List<Double> getRtValues() {
        return rtValues;
    }

    public void setRtValues(List<Double> rtValues) {
        this.rtValues = rtValues != null ? new ArrayList<>(rtValues) : new ArrayList<>();
    }

    public ExperimentParameters getExperimentParameters() {
        return experimentParameters;
    }

    public void setExperimentParameters(ExperimentParameters experimentParameters) {
        this.experimentParameters = experimentParameters;
    }

    public List<Map<Double, Double>> getCompositeSpectrum() {
        return compositeSpectrum;
    }

    public void setCompositeSpectrum(List<Map<Double, Double>> compositeSpectrum) {
        this.compositeSpectrum = compositeSpectrum != null ? new ArrayList<>(compositeSpectrum) : new ArrayList<>();
    }
}
