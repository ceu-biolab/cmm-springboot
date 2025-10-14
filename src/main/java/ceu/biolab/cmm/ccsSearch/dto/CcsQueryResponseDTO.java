package ceu.biolab.cmm.ccsSearch.dto;

import lombok.Data;

@Data
public class CcsQueryResponseDTO {
    private int compoundId;
    private String compoundName;
    private double monoisotopicMass;
    private double dbCcs;
    private String formula;
    private Integer compoundType;
    private Double logP;
    private Integer pathwayId;
    private String pathwayName;
    private String pathwayMap;

    public CcsQueryResponseDTO(int compoundId, String compoundName, double monoisotopicMass, double dbCcs, String formula, String formulaType, Integer compoundType, Double logP, String pathwayName, Integer pathwayId, String pathwayMap) {
        this.compoundId = compoundId;
        this.compoundName = compoundName;
        this.monoisotopicMass = monoisotopicMass;
        this.dbCcs = dbCcs;
        this.formula = formula;
        this.compoundType = compoundType;
        this.logP = logP;
        this.pathwayName = pathwayName;
        this.pathwayId = pathwayId;
        this.pathwayMap = pathwayMap;
    }

    public CcsQueryResponseDTO() {
        this.compoundId = -1;
        this.compoundName = "";
        this.monoisotopicMass = 0.0;
        this.dbCcs = 0.0;
        this.formula = "";
        this.compoundType = null;
        this.logP = null;
        this.pathwayName = "";
        this.pathwayId = -1;
        this.pathwayMap = "";
    }
}
