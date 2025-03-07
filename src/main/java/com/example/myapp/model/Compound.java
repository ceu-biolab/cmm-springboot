package com.example.myapp.model;

public class Compound {
    private String compoundName;
    private Double mass;

    // Constructor
    public Compound(String compoundName, Double mass) {
        this.compoundName = compoundName;
        this.mass = mass;
    }

    public Compound() {}

    // Getters and setters
    public String getCompoundName() {
        return compoundName;
    }

    public void setCompoundName(String compoundName) {
        this.compoundName = compoundName;
    }

    public Double getMass() {
        return mass;
    }

    public void setMass(Double mass) {
        this.mass = mass;
    }
}
