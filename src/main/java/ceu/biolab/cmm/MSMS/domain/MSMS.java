package ceu.biolab.cmm.MSMS.domain;

import ceu.biolab.cmm.shared.domain.IonizationMode;

import java.util.ArrayList;
import java.util.List;
//TODO creado esta clase pq no se muy bien a dnd tienen que ir los espectros encontrados y no he encontrado ninguna clase con esos parametros
public class MSMS {
    int compoundId;
    int msmsId;
    IonizationMode ionizationMode;
    CIDEnergy voltageEnergy;
    List<Peak> peaks;

    public MSMS(int compoundId, int msmsId, IonizationMode ionizationMode, CIDEnergy voltageEnergy, List<Peak> peaks) {
        this.compoundId = compoundId;
        this.msmsId = msmsId;
        this.ionizationMode = ionizationMode;
        this.voltageEnergy = voltageEnergy;
        this.peaks = peaks;
    }

    public MSMS() {
        this.compoundId = 0;
        this.msmsId = 0;
        this.ionizationMode = IonizationMode.NEGATIVE;
        this.voltageEnergy = CIDEnergy.E10;
        this.peaks = new ArrayList<>();
    }

    public int getCompoundId() {
        return compoundId;
    }

    public void setCompoundId(int compoundId) {
        this.compoundId = compoundId;
    }

    public int getMsmsId() {
        return msmsId;
    }

    public void setMsmsId(int msmsId) {
        this.msmsId = msmsId;
    }

    public IonizationMode getIonizationMode() {
        return ionizationMode;
    }

    public void setIonizationMode(IonizationMode ionizationMode) {
        this.ionizationMode = ionizationMode;
    }

    public CIDEnergy getVoltageEnergy() {
        return voltageEnergy;
    }

    public void setVoltageEnergy(CIDEnergy voltageEnergy) {
        this.voltageEnergy = voltageEnergy;
    }

    public List<Peak> getPeaks() {
        return peaks;
    }

    public void setPeaks(List<Peak> peaks) {
        this.peaks = peaks;
    }
}
