package ceu.biolab.cmm.adapters.msmsSearchAdapter.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import ceu.biolab.cmm.adapters.shared.domain.SpectraType;
import lombok.Builder;
import lombok.Data;

/**
 * MS/MS Search adapter response DTO.
 *
 * Mirrors the legacy MS/MS Search Service response:
 * an array of MS/MS_compound_object entries under "results".
 */
@Data
@Builder
public class MsmsSearchAdapterResponseDTO {

	@JsonProperty("results")
	private List<MsmsCompound> results;

	@Data
	@Builder
	@JsonPropertyOrder({
			"spectral_display_tools",
			"identifier",
			"hmdb_compound",
			"hmdb_uri",
			"name",
			"formula",
			"mass",
			"score"
	})
	public static class MsmsCompound {

		// spectra_type_enum (e.g. "Experimental", "Predicted")
		@JsonProperty("spectral_display_tools")
		private SpectraType spectralDisplayTools;

		private Integer identifier;

		@JsonProperty("hmdb_compound")
		private String hmdbCompound;

		@JsonProperty("hmdb_uri")
		private String hmdbUri;

		private String name;

		private String formula;

		// Neutral mass of the compound
		@JsonProperty("mass")
		private Double mass;

		// Score as a double (e.g., cosine similarity)
		@JsonProperty("score")
		private Double score;
	}
}


