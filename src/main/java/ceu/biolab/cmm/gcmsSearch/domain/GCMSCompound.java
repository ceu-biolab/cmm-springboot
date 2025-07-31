package ceu.biolab.cmm.gcmsSearch.domain;

import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
public class GCMSCompound extends Compound {

    // GET ALL THE INFORMATION FROM THE DATABASE
    // ENUM DERIVADO
    // ENUM COLUMN_TYPE
    // PARA ESE DERIVADO TIENE UN RI de BBDDy

    //TODO añadir el resto ->not here -> is at Compound

    private double dbRI;
    //private double dbRT;
    private DerivatizationMethod derivatizationMethod;
    private ColumnType gcColumn;

    //WITH MY CONFIGURATION A COMPOUND WILL ONLY HAVE ONE SPECTRUM PER DERIVATIZATION METHOD TYPE
    private List<Spectrum> GCMSSpectrum;

    //private int gcmsSpectrumId; //Lo necesito??? -> si -> lo puse en Spectrum

    /*private double mz;
    private double intensity;*/


    /*public GCMSCompound(Compound compound, double RI, double RT, String dertype,
                        String gcColumn, List<Spectrum> GCMS_Spectrum) {
        super(compound.getCompoundId(), compound.getCasId(), compound.getCompoundName(), compound.getFormula(),
                compound.getMass(), compound.getChargeTpe(), compound.getChargeNumber(), compound.getFormulaType(),
                compound.getCompoundType(), compound.getCompoundStatus(), compound.getFormulaTypeInt(),
                compound.getLogP(), compound.getRtPred(), compound.getInchi(), compound.getInchiKey(),
                compound.getSmiles(), compound.getLipidType(), compound.getNumChains(), compound.getNumCarbons(),
                compound.getDoubleBonds(), compound.getBiologicalActivity(), compound.getMeshNomenclature(),
                compound.getIupacClassification(), compound.getMol2(), compound.getPathways());
        this.dbRI = RI;
        this.dbRT = RT;
        seeDerType(dertype);
        seeColumnType(gcColumn);
        this.GCMSSpectrum = GCMS_Spectrum;
    }*/

}
