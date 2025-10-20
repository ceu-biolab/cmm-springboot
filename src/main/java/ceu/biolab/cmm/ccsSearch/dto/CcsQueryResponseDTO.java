package ceu.biolab.cmm.ccsSearch.dto;

import lombok.Data;

@Data
public class CcsQueryResponseDTO {
    private int compoundId;
    private String casId;
    private String compoundName;
    private String formula;
    private double monoisotopicMass;
    private Integer chargeType;
    private Integer chargeNumber;
    private Integer formulaTypeInt;
    private Integer compoundType;
    private Double logP;
    private Double rtPred;
    private String biologicalActivity;
    private String meshNomenclature;
    private String iupacClassification;
    private String inchi;
    private String inchiKey;
    private String smiles;
    private String lipidType;
    private Integer numChains;
    private Integer numberCarbons;
    private Integer doubleBonds;
    private String category;
    private String mainClass;
    private String subClass;
    private String classLevel4;
    private double dbCcs;
    private Integer pathwayId;
    private String pathwayName;
    private String pathwayMap;
}
