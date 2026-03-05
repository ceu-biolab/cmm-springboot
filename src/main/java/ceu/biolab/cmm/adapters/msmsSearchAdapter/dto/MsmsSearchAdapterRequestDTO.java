package ceu.biolab.cmm.adapters.msmsSearchAdapter.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import ceu.biolab.cmm.adapters.shared.domain.LegacyIonMode;
import ceu.biolab.cmm.adapters.shared.domain.LegacyIonizationVoltage;
import ceu.biolab.cmm.adapters.shared.domain.SpectraType;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.msFeature.Peak;

/**
 * Adapter-layer DTO for the legacy MS/MS Search request.
 *
 * Mirrors the Table 20 specification (ion_mass, ms_ms_peaks, tolerances,
 * ion_mode, Ionization_voltage, spectra_types) while relying on shared
 * enums where they already exist.
 */
@Data
public class MsmsSearchAdapterRequestDTO {

	// === DEFAULT VALUES ===
	private static final double DEFAULT_TOLERANCE = 10.0;
	private static final MzToleranceMode DEFAULT_TOLERANCE_MODE = MzToleranceMode.PPM;
	private static final LegacyIonMode DEFAULT_ION_MODE = LegacyIonMode.POSITIVE;
	private static final LegacyIonizationVoltage DEFAULT_IONIZATION_VOLTAGE = LegacyIonizationVoltage.LOW;
	private static final SpectraType DEFAULT_SPECTRA_TYPE = SpectraType.EXPERIMENTAL;

	// === FIELDS ===

	/**
	 * Precursor ion mass (m/z).
	 * In the legacy spec this is "ion_mass".
	 * No default – required for a meaningful search.
	 */
	@JsonProperty("ion_mass")
	@Positive(message = "ion_mass must be greater than 0")
	private double ionMass;

	/**
	 * Experimental MS/MS peaks (m/z, intensity pairs).
	 * In the legacy spec this is "ms_ms_peaks" (array of MS_MS_Peak).
	 */
	@JsonProperty("ms_ms_peaks")
	private List<Peak> msMsPeaks;

	/**
	 * Precursor ion tolerance (range [0..100]).
	 * Legacy name: "precursor_ion_tolerance".
	 */
	@JsonProperty("precursor_ion_tolerance")
	@PositiveOrZero(message = "precursor_ion_tolerance must be zero or positive")
	@Max(value = 100, message = "precursor_ion_tolerance must not exceed 100")
	private double precursorIonTolerance;

	/**
	 * Precursor ion tolerance mode ("ppm" or "mDa").
	 * Legacy name: "precursor_ion_tolerance_mode".
	 */
	@JsonProperty("precursor_ion_tolerance_mode")
	@JsonDeserialize(using = ceu.biolab.cmm.shared.domain.MzToleranceModeDeserializer.class)
	private MzToleranceMode precursorIonToleranceMode;

	/**
	 * Fragment m/z tolerance (range [0..100]).
	 * Legacy name: "precursor_mz_tolerance".
	 */
	@JsonProperty("precursor_mz_tolerance")
	@PositiveOrZero(message = "precursor_mz_tolerance must be zero or positive")
	//@Max(value = 100, message = "precursor_mz_tolerance must not exceed 100")
	private double precursorMzTolerance;

	/**
	 * Fragment m/z tolerance mode ("ppm" or "mDa").
	 * Legacy name: "precursor_mz_tolerance_mode".
	 */
	@JsonProperty("precursor_mz_tolerance_mode")
	@JsonDeserialize(using = ceu.biolab.cmm.shared.domain.MzToleranceModeDeserializer.class)
	private MzToleranceMode precursorMzToleranceMode;

	/**
	 * Ionization mode ("positive" / "negative").
	 * Legacy name: "ion_mode".
	 */
	@JsonProperty("ion_mode")
	@JsonDeserialize(using = ceu.biolab.cmm.adapters.shared.deserializer.LegacyIonModeDeserializer.class)
	private LegacyIonMode ionMode;

	/**
	 * Ionization / collision voltage: "low", "medium", "high", "all".
	 * Legacy name: "Ionization_voltage".
	 */
	@JsonProperty("Ionization_voltage")
	@JsonDeserialize(using = ceu.biolab.cmm.adapters.shared.deserializer.LegacyIonizationVoltageDeserializer.class)
	private LegacyIonizationVoltage ionizationVoltage;

	/**
	 * Types of spectra provided ("experimental", "predicted").
	 * Legacy name: "spectra_types".
	 */
	@JsonProperty("spectra_types")
	@JsonDeserialize(contentUsing = ceu.biolab.cmm.adapters.shared.deserializer.SpectraTypeDeserializer.class)
	private List<SpectraType> spectraTypes;


	// === CONSTRUCTORS ===

	public MsmsSearchAdapterRequestDTO() {
		this.ionMass = 0.0;
		this.msMsPeaks = new ArrayList<>();
		this.precursorIonTolerance = DEFAULT_TOLERANCE;
		this.precursorIonToleranceMode = DEFAULT_TOLERANCE_MODE;
		this.precursorMzTolerance = DEFAULT_TOLERANCE;
		this.precursorMzToleranceMode = DEFAULT_TOLERANCE_MODE;
		this.ionMode = DEFAULT_ION_MODE;
		this.ionizationVoltage = DEFAULT_IONIZATION_VOLTAGE;
		this.spectraTypes = List.of(DEFAULT_SPECTRA_TYPE);
	}
}
