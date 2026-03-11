package ceu.biolab.cmm.adapters.browseSearchAdapter.dto;

import lombok.Data;


import ceu.biolab.cmm.shared.domain.compound.Pathway;
import lombok.Builder;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
public class BrowseSearchAdapterResponseDTO {

	@JsonProperty("results")
	private List<TheoreticalCompound> theoreticalCompounds;

	@Data
	@Builder
	public static class TheoreticalCompound {
		private Integer identifier;
		private String name;
		private String formula;

		@JsonProperty("molecular_weight")
		private Double molecularWeight;
		private String cas;

		@JsonProperty("hmdb_compound")
		private String hmdbCompound;

		@JsonProperty("hmdb_uri")
		private String hmdbUri;

		@JsonProperty("metlin_compound")
		private String metlinCompound;

		@JsonProperty("metlin_uri")
		private String metlinUri;

		@JsonProperty("lipidmaps_compound")
		private String lipidmapsCompound;

		@JsonProperty("lipidmaps_uri")
		private String lipidmapsUri;

		@JsonProperty("kegg_compound")
		private String keggCompound;

		@JsonProperty("kegg_uri")
		private String keggUri;

		@JsonProperty("pubchem_compound")
		private String pubchemCompound;

		@JsonProperty("pubchem_uri")
		private String pubchemUri;

		private String inChiKey;
		private List<Pathway> pathways;

	}
}
