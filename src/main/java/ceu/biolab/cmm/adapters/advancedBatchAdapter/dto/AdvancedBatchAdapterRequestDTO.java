package ceu.biolab.cmm.adapters.advancedBatchAdapter.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.adapters.shared.domain.LegacyDatabase;
import ceu.biolab.cmm.adapters.shared.domain.LegacyMassesMode;
import ceu.biolab.cmm.adapters.shared.domain.LegacyMetaboliteType;
import ceu.biolab.cmm.adapters.shared.dto.SpectrumDTO;

/**
 * Data Transfer Object for Advanced Batch Adapter requests.
 *
 * Represents a multi-compound advanced batch search with optional
 * chromatographic and spectral information.
 */
@Data
public class AdvancedBatchAdapterRequestDTO {

	// === DEFAULTS ===

	private static final FormulaType DEFAULT_CHEMICAL_ALPHABET = FormulaType.CHNOPS;
	private static final boolean DEFAULT_DEUTERIUM = false;
	private static final String DEFAULT_MODIFIERS_TYPE = "none";
	private static final MzToleranceMode DEFAULT_TOLERANCE_MODE = MzToleranceMode.PPM;
	private static final double DEFAULT_TOLERANCE = 10.0;
	private static final LegacyMassesMode DEFAULT_MASSES_MODE = LegacyMassesMode.MZ;
	private static final IonizationMode DEFAULT_ION_MODE = IonizationMode.POSITIVE;
	private static final LegacyMetaboliteType DEFAULT_METABOLITE_TYPE = LegacyMetaboliteType.ALL_EXCEPT_PEPTIDES;

	// === FIELDS ===

	/**
	 * Chemical alphabet used for formula generation.
	 * Maps from chemical_alphabet_enum, default "CHNOPS".
	 */
	@JsonProperty("chemical_alphabet")
	private FormulaType chemicalAlphabet;

	/**
	 * Whether deuterium is included in the alphabet.
	 * Default: false.
	 */
	private boolean deuterium;

	/**
	 * Type of modifier applied.
	 * Maps from modifiers_type_enum, default "none".
	 */
	@JsonProperty("modifiers_type")
	private String modifiersType;

	// Simple-search-like parameters reused for internal LCMS request

	@JsonProperty("metabolites_type")
	@com.fasterxml.jackson.databind.annotation.JsonDeserialize(
			using = ceu.biolab.cmm.adapters.shared.deserializer.LegacyMetaboliteTypeDeserializer.class)
	private LegacyMetaboliteType metaboliteTypes;

	@com.fasterxml.jackson.databind.annotation.JsonDeserialize(contentUsing = ceu.biolab.cmm.adapters.shared.deserializer.LegacyDatabaseDeserializer.class)
	private Set<LegacyDatabase> databases;

	@JsonProperty("masses_mode")
	@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = ceu.biolab.cmm.adapters.shared.deserializer.LegacyMassesModeDeserializer.class)
	private LegacyMassesMode massesMode;

	@JsonProperty("ion_mode")
	@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = ceu.biolab.cmm.shared.domain.IonizationModeDeserializer.class)
	private IonizationMode ionMode;

	private Set<String> adducts;

	private Double tolerance;

	@JsonProperty("tolerance_mode")
	@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = ceu.biolab.cmm.shared.domain.MzToleranceModeDeserializer.class)
	private MzToleranceMode toleranceMode;

	// Primary masses list (called "masses" in JSON)
	@JsonProperty("masses")
	private List<Double> masses;

	/**
	 * Retention times corresponding to the primary spectra/masses.
	 * Default: empty array.
	 */
	@JsonProperty("retention_times")
	private List<Double> retentionTimes;

	/**
	 * Composite spectra for each queried compound.
	 * JSON: array of arrays of spectra_object.
	 * Default: empty.
	 */
	@JsonProperty("composite_spectra")
	private List<List<SpectrumDTO>> compositeSpectra;

	/**
	 * All masses considered in the batch.
	 * Default: empty array.
	 */
	@JsonProperty("all_masses")
	private List<Double> allMasses;

	/**
	 * Retention times aligned with all_masses.
	 * Default: empty array.
	 */
	@JsonProperty("all_retention_times")
	private List<Double> allRetentionTimes;

	/**
	 * Composite spectra aligned with all_masses / all_retention_times.
	 * JSON: array of arrays of spectra_object.
	 * Default: empty.
	 */
	@JsonProperty("all_composite_spectra")
	private List<List<SpectrumDTO>> allCompositeSpectra;


	// === CONSTRUCTORS ===

	/**
	 * Default constructor – initializes all fields with the specification defaults.
	 */
	public AdvancedBatchAdapterRequestDTO() {
		this.chemicalAlphabet = DEFAULT_CHEMICAL_ALPHABET;
		this.deuterium = DEFAULT_DEUTERIUM;
		this.modifiersType = DEFAULT_MODIFIERS_TYPE;
		this.metaboliteTypes = DEFAULT_METABOLITE_TYPE;
		// "all-except-mine" in the legacy world maps to ALL_EXCEPT_MINE,
		// which in the new backend is equivalent to ALL.
		this.databases = java.util.Set.copyOf(java.util.List.of(LegacyDatabase.ALL_EXCEPT_MINE));
		this.massesMode = DEFAULT_MASSES_MODE;
		this.ionMode = DEFAULT_ION_MODE;
		// Default adducts depend on ion mode and are expanded later;
		// an empty set here signals "use defaults".
		this.adducts = java.util.Set.of();
		this.tolerance = DEFAULT_TOLERANCE;
		this.toleranceMode = DEFAULT_TOLERANCE_MODE;
		this.masses = new ArrayList<>();
		this.retentionTimes = new ArrayList<>();
		this.compositeSpectra = new ArrayList<>();
		this.allMasses = new ArrayList<>();
		this.allRetentionTimes = new ArrayList<>();
		this.allCompositeSpectra = new ArrayList<>();
	}

	public AdvancedBatchAdapterRequestDTO(
			FormulaType chemicalAlphabet,
			boolean deuterium,
			String modifiersType,
			List<Double> retentionTimes,
			List<List<SpectrumDTO>> compositeSpectra,
			List<Double> allMasses,
			List<Double> allRetentionTimes,
			List<List<SpectrumDTO>> allCompositeSpectra) {

		this.chemicalAlphabet = chemicalAlphabet != null ? chemicalAlphabet : DEFAULT_CHEMICAL_ALPHABET;
		this.deuterium = deuterium;
		this.modifiersType = modifiersType != null ? modifiersType : DEFAULT_MODIFIERS_TYPE;
		this.retentionTimes = retentionTimes != null ? retentionTimes : new ArrayList<>();
		this.compositeSpectra = compositeSpectra != null ? compositeSpectra : new ArrayList<>();
		this.allMasses = allMasses != null ? allMasses : new ArrayList<>();
		this.allRetentionTimes = allRetentionTimes != null ? allRetentionTimes : new ArrayList<>();
		this.allCompositeSpectra = allCompositeSpectra != null ? allCompositeSpectra : new ArrayList<>();
	}
}
