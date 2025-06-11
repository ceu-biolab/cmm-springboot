package ceu.biolab.cmm.MSMS.domain;

import ceu.biolab.cmm.shared.domain.compound.Compound;

import java.util.ArrayList;
import java.util.List;
//TODO creado esta clase pq no se muy bien a dnd tienen que ir los espectros encontrados y no he encontrado ninguna clase con esos parametros
public class MSMSAnotation extends Compound implements Comparable<MSMSAnotation> {

    int msmsId;
    List<Peak> peaks;
    Double score;
    Double precursorMz;

    public MSMSAnotation(int msmsId, List<Peak> peaks, Double score, Double mass) {
        super(new Compound());
        this.msmsId = msmsId;
        this.peaks = peaks;
        this.score = score;
        this.precursorMz = mass;
    }
    public MSMSAnotation(Compound compound) {
        super(compound);
        this.msmsId = 0;
        this.peaks = new ArrayList<>();
        this.score = 0.0;
        this.precursorMz = 0.0;
    }
    public MSMSAnotation() {
        super(new Compound());
        this.msmsId  = 0;
        this.peaks = new ArrayList<>();
        this.score = 0.0;
        this.precursorMz = 0.0;
    }

    public int getMsmsId() {
        return msmsId;
    }

    public void setMsmsId(int msmsId) {
        this.msmsId = msmsId;
    }


    public List<Peak> getPeaks() {
        return peaks;
    }

    public void setPeaks(List<Peak> peaks) {
        this.peaks = peaks;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getPrecursorMz() {
        return precursorMz;
    }
    public void setPrecursorMz(double precursorMz) {
        this.precursorMz = precursorMz;
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
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MSMSAnotation)) return false;
        MSMSAnotation other = (MSMSAnotation) o;
        return( this.msmsId == other.msmsId&& this.getCompoundId()==other.getCompoundId() );
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(msmsId);
    }
    @Override
    public String toString() {
        if (this.getMsmsId() != 0) {
            return "MSMS{" +
                    ", msmsId=" + msmsId +
                    ", peaks=" + peaks +
                    ", score=" + score +
                    '}';
        }
        else return "empty MSMS";
    }



}
