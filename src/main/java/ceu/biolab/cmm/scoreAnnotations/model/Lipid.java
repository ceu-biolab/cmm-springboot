package ceu.biolab.cmm.scoreAnnotations.model;

import ceu.biolab.cmm.shared.domain.compound.Compound;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class Lipid extends Compound {
    public String lipidType;
    public int numberChains;
    public int numberCarbons;
    public int numberDoubleBonds;
}
