package com.example.myapp.model.ccsMatcher;

public class CompoundCcsDTO {
    private String compoundName;
    private double compoundCcs;

    public CompoundCcsDTO() {
    }

    public CompoundCcsDTO(String compoundName, double compoundCcs) {
        this.compoundName = compoundName;
        this.compoundCcs = compoundCcs;
    }

    public String getCompoundName() {
        return compoundName;
    }

    public void setCompoundName(String compoundName) {
        this.compoundName = compoundName;
    }

    public double getCompoundCcs() {
        return compoundCcs;
    }

    public void setCompoundCcs(double compoundCcs) {
        this.compoundCcs = compoundCcs;
    }

    @Override
    public String toString() {
        return "CompoundCcsDTO{" +
               "compoundName='" + compoundName + '\'' +
               ", compoundCcs=" + compoundCcs +
               '}';
    }
}
