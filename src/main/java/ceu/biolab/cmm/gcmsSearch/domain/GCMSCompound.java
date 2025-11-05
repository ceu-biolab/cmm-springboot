package ceu.biolab.cmm.gcmsSearch.domain;

import ceu.biolab.cmm.shared.domain.compound.CMMCompound;
import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class GCMSCompound extends CMMCompound {

    private double dbRI;
    private DerivatizationMethod derivatizationMethod;
    private ColumnType gcColumn;

    private List<Spectrum> GCMSSpectrum;

}
