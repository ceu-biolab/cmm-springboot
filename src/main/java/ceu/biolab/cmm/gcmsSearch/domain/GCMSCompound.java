package ceu.biolab.cmm.gcmsSearch.domain;

import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
public class GCMSCompound extends Compound {

    private double dbRI;
    private DerivatizationMethod derivatizationMethod;
    private ColumnType gcColumn;

    private List<Spectrum> GCMSSpectrum;

}
