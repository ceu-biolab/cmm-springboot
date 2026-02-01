package ceu.biolab.cmm.adapters.simpleSearchAdapter.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class SimpleSearchAdapterResponseDTO {
    
    private int identifier;

    private double EM;

    private String name;

    private String formula;

    @JsonProperty("adduct")
    private Set<String> adducts;

    @JsonProperty("molecular_weight")
    private double molecularWeight;

    @JsonProperty("error_ppm")
    private Integer errorPpm;

    private Integer ionizationScore;   // Must be -2 for simple search
    private Integer finalScore;     // Must be -2 for simple search

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

    private List<String> pathways;

    private String inChiKey;
}

    

