package ceu.biolab.cmm.shared.domain;

import ceu.biolab.FormulaType;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class Compound {
    private int compoundId;
    private String casId;
    private String compoundName;
    private String formula;
    private double mass;
    private int chargeType;
    private int chargeNumber;
    private FormulaType formulaType;
    private int compoundType;
    private int compoundStatus;
    private int logP;
}
