package ceu.biolab.cmm.MSMS.domain;

import ceu.biolab.cmm.shared.domain.compound.Compound;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
//TODO creado esta clase pq no se muy bien a dnd tienen que ir los espectros encontrados y no he encontrado ninguna clase con esos parametros

@Data
public class MSMSAnotation extends Compound implements Comparable<MSMSAnotation> {

    private int msmsId;
    private Spectrum peaks;
    private Double score;
    private Double precursorMz;

    public MSMSAnotation(int msmsId, Spectrum peaks, Double score, Double mass) {
        super(new Compound());
        this.msmsId = msmsId;
        this.peaks = peaks;
        this.score = score;
        this.precursorMz = mass;
    }
    public MSMSAnotation(Compound compound) {
        super(compound);
        this.msmsId = 0;
        this.peaks = new Spectrum();
        this.score = 0.0;
        this.precursorMz = 0.0;
    }
    public MSMSAnotation() {
        super(new Compound());
        this.msmsId  = 0;
        this.peaks = new Spectrum();
        this.score = 0.0;
        this.precursorMz = 0.0;
    }

    public MSMSAnotation getMSMS(){
        return new MSMSAnotation(this);
    }

    public int compareTo(MSMSAnotation o) {
        // Por ejemplo: ordena por msmsId
        return Integer.compare(this.msmsId, o.msmsId);
        // O podrías ordenar por score:
        // return Double.compare(o.score, this.score); // descendente
    }

}
