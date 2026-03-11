package ceu.biolab.cmm.adapters.shared.domain;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Spectra type enumeration used in MS/MS related adapter requests.
 *
 * Matches spectra_type_enum: "experimental", "predicted" (case-insensitive).
 */
public enum SpectraType {

	EXPERIMENTAL("Experimental"),
	PREDICTED("Predicted");

	private final String value;

	SpectraType(String value) {
		this.value = value;
	}

	@JsonValue
	public String getValue() {
		return value;
	}

	public static SpectraType fromString(String value) {
		if (value == null) {
			return EXPERIMENTAL;
		}
		String normalized = value.trim().toLowerCase();
		if (normalized.isEmpty()) {
			return EXPERIMENTAL;
		}
		switch (normalized) {
			case "experimental":
				return EXPERIMENTAL;
			case "predicted":
				return PREDICTED;
			default:
				throw new IllegalArgumentException("Invalid SpectraType: " + value);
		}
	}
}
