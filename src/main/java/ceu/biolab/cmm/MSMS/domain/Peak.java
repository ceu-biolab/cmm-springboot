package ceu.biolab.cmm.MSMS.domain;

public class Peak {
    double mz;
    int intensity;

    public Peak(double mz, int intensity) {
        this.mz = mz;
        this.intensity = intensity;
    }

    public double getMz() {
        return mz;
    }

    public void setMz(double mz) {
        this.mz = mz;
    }

    public int getIntensity() {
        return intensity;
    }

    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }
}

