package ceu.biolab.cmm.adapters.simpleSearchAdapter.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class SimpleSearchAdapterResponseDTO {

    @JsonProperty("results")
    private List<Result> results;

    @Data
    @JsonPropertyOrder({
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
    public static class Result {

        private int identifier;

        @JsonProperty("EM")
        private double EM;

        private String name;

        private String formula;

        @JsonProperty("adduct")
        private String adduct;

        @JsonProperty("molecular_weight")
        private double molecularWeight;

        @JsonProperty("error_ppm")
        private Integer errorPpm;

        private Double ionizationScore;   // Must be -2.0 for simple search
        private Double finalScore;        // Must be -2.0 for simple search

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



