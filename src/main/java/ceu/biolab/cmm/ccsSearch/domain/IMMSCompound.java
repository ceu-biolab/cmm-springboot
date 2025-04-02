package ceu.biolab.cmm.ccsSearch.domain;

import ceu.biolab.cmm.shared.domain.Pathway;

import java.util.ArrayList;
import java.util.List;

// TODO inherit compound from shared domain
public class IMMSCompound {
    private int compoundId;
    private String compoundName;
    private double monoisotopicMass;
    private double dbCcs;
    private String formula;
    private String formulaType;
    // TODO compoundType should be a String or enum
    private int compoundType;
    private Double logP;
    private List<Pathway> pathways;

    public IMMSCompound(int compoundId, String compoundName, double monoisotopicMass, double dbCcs, String formula,
            String formulaType, int compoundType, Double logP, List<Pathway> pathways) {
        this.compoundId = compoundId;
        this.compoundName = compoundName;
        this.monoisotopicMass = monoisotopicMass;
        this.dbCcs = dbCcs;
        this.formula = formula;
        this.formulaType = formulaType;
        this.compoundType = compoundType;
        this.logP = logP;
        this.pathways = pathways;
    }

    public IMMSCompound() {
        this.compoundId = -1;
        this.compoundName = "";
        this.monoisotopicMass = 0.0;
        this.dbCcs = 0.0;
        this.formula = "";
        this.formulaType = "";
        this.compoundType = -1;
        this.logP = null;
        this.pathways = new ArrayList<>();
    }

    public void addPathway(Pathway pathway) {
        if (pathway != null && pathway.getPathwayId() != -1) {
            this.pathways.add(pathway);
        }
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

    public List<Pathway> getPathways() {
        return pathways;
    }

    public void setPathways(List<Pathway> pathways) {
        if (pathways == null) {
            this.pathways = new ArrayList<>();
        } else {
            this.pathways = new ArrayList<>(pathways);
        }
    }

    @Override
    public String toString() {
        return "IMMSFeature [compoundId=" + compoundId + ", compoundName=" + compoundName + ", monoisotopicMass="
                + monoisotopicMass + ", dbCcs=" + dbCcs + ", formula=" + formula + ", formulaType=" + formulaType
                + ", compoundType=" + compoundType + ", logP=" + logP + ", pathways=" + pathways + "]";
    }
}
