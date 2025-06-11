package ceu.biolab.cmm.MSMS.domain;

import lombok.Setter;

import java.util.Objects;

public class Peak {
   Double mz;
   Double intensity ;


    public Peak(Double mz, Double intensity) {
        this.mz = mz;
        this.intensity = intensity;
    }

    public Peak() {
        this.mz = null;
        this.intensity = null;
    }

    public Double getMz() {
        return mz;
    }

    public void setMz(Double mz) {
        this.mz = mz;
    }

    public void setIntensity(Double intensity) {
        this.intensity = intensity;
    }

    public Double getIntensity() {
        return intensity;
    }

    @Override
    public String toString() {
        return "Peak{" +
                "mz=" + mz +
                ", intensity=" + intensity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Peak peak = (Peak) o;
        return Objects.equals(mz, peak.mz) && Objects.equals(intensity, peak.intensity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mz, intensity);
    }



}
