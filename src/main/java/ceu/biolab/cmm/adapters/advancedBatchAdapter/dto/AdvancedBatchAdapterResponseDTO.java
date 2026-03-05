package ceu.biolab.cmm.adapters.advancedBatchAdapter.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

/**
 * Response DTO for Advanced Batch adapter.
 *
 * Encapsulates an array of putative_annotation_object items under
 * the "results" property, matching the specified JSON structure.
 */
@Data
public class AdvancedBatchAdapterResponseDTO {

	@JsonProperty("results")
	private List<PutativeAnnotation> results;

	@Data
	@JsonPropertyOrder({
			"RT",
			"adductRelationScore",
			"RTscore",
			"identifier",
			"EM",
			"name",
			"formula",
			"adduct",
			"molecular_weight",
			"error_ppm",
			"ionizationScore",
			"finalScore",
			"cas",
			"kegg_compound",
			"kegg_uri",
			"hmdb_compound",
			"hmdb_uri",
			"lipidmaps_compound",
			"lipidmaps_uri",
			"metlin_compound",
			"metlin_uri",
			"pubchem_compound",
			"pubchem_uri",
			"inChiKey",
			"pathways"
	})
	public static class PutativeAnnotation {

		@JsonProperty("RT")
		private double rt;

		private int adductRelationScore; // -2 or [0..2]

		@JsonProperty("RTscore")
		private int rtScore; // -2 or [0..2]

		private int identifier;

		@JsonProperty("EM")
		private double em;

		private String name;

		private String formula;

		private String adduct;

		@JsonProperty("molecular_weight")
		private double molecularWeight;

		@JsonProperty("error_ppm")
		private Integer errorPpm;

		private Integer ionizationScore; // -2 or [0..2]

		@JsonProperty("finalScore")
		private Integer finalScore; // -2 or [0..2]

		private String cas;

		@JsonProperty("kegg_compound")
		private String keggCompound;

		@JsonProperty("kegg_uri")
		private String keggUri;

		@JsonProperty("hmdb_compound")
		private String hmdbCompound;

		@JsonProperty("hmdb_uri")
		private String hmdbUri;

		@JsonProperty("lipidmaps_compound")
		private String lipidmapsCompound;

		@JsonProperty("lipidmaps_uri")
		private String lipidmapsUri;

		@JsonProperty("metlin_compound")
		private String metlinCompound;

		@JsonProperty("metlin_uri")
		private String metlinUri;

		@JsonProperty("pubchem_compound")
		private String pubchemCompound;

		@JsonProperty("pubchem_uri")
		private String pubchemUri;

		private String inChiKey;

		private List<String> pathways;
	}
}

