package ceu.biolab.cmm.ccsSearch.dto;

// TODO inherit compound from shared domain
public class CcsQueryResponseDTO {
    private int compoundId;
    private String compoundName;
    private double monoisotopicMass;
    private double dbCcs;
    private String formula;
    private String formulaType;
    // TODO compoundType should be a String or enum
    private int compoundType;
    private Double logP;
    private Integer pathwayId;
    private String pathwayName;
    private String pathwayMap;

    public CcsQueryResponseDTO(int compoundId, String compoundName, double monoisotopicMass, double dbCcs, String formula, String formulaType, int compoundType, Double logP, String pathwayName, Integer pathwayId, String pathwayMap) {
        this.compoundId = compoundId;
        this.compoundName = compoundName;
        this.monoisotopicMass = monoisotopicMass;
        this.dbCcs = dbCcs;
        this.formula = formula;
        this.formulaType = formulaType;
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
        this.formulaType = "";
        this.compoundType = -1;
        this.logP = null;  // Changed from 0.0 to null
        this.pathwayName = "";
        this.pathwayId = -1;
        this.pathwayMap = "";
    }

    public double getMonoisotopicMass() {
        return monoisotopicMass;
    }

    public void setMonoisotopicMass(double mzValue) {
        this.monoisotopicMass = mzValue;
    }

    public double getDbCcs() {
        return dbCcs;
    }

    public void setDbCcs(double ccsValue) {
        this.dbCcs = ccsValue;
    }

    public String getCompoundName() {
        return compoundName;
    }

    public void setCompoundName(String compoundName) {
        this.compoundName = compoundName;
    }
    
    public int getCompoundId() {
        return compoundId;
    }

    public void setCompoundId(int compoundId) {
        this.compoundId = compoundId;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getFormulaType() {
        return formulaType;
    }

    public void setFormulaType(String formulaType) {
        this.formulaType = formulaType;
    }

    public int getCompoundType() {
        return compoundType;
    }

    public void setCompoundType(int compoundType) {
        this.compoundType = compoundType;
    }

    public Double getLogP() {
        return logP;
    }

    public void setLogP(Double logP) {
        this.logP = logP;
    }

    public String getPathwayName() {
        return pathwayName;
    }

    public void setPathwayName(String pathwayName) {
        this.pathwayName = pathwayName;
    }
    
    public Integer getPathwayId() {
        return pathwayId;
    }

    public void setPathwayId(Integer pathwayId) {
        this.pathwayId = pathwayId;
    }

    public String getPathwayMap() {
        return pathwayMap;
    }

    public void setPathwayMap(String pathwayMap) {
        this.pathwayMap = pathwayMap;
    }

    @Override
    public String toString() {
        return "CcsQueryResponse [mzValue=" + monoisotopicMass + ", ccsValue=" + dbCcs + ", compoundName=" + compoundName + 
               ", compoundId=" + compoundId + ", formula=" + formula + ", formulaType=" + formulaType + 
               ", compoundType=" + compoundType + ", logP=" + logP + ", pathway=" + pathwayName + 
               ", pathwayId=" + pathwayId + ", pathwayMap=" + pathwayMap + "]";
    }
}
