package ceu.biolab.cmm.scoreAnnotations.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import ceu.biolab.cmm.shared.domain.Compound;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class Lipid extends Compound {
    public String lipidType;
    public int numberChains;
    public int numberCarbons;
    public int numberDoubleBonds;
}
