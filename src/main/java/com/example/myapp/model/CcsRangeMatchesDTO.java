package com.example.myapp.model;

import java.util.List;

public class CcsRangeMatchesDTO {
    private double value;
    private double tolerance;
    private double tolerancePercentage;
    private List<CompoundCcsDTO> matches;

    public CcsRangeMatchesDTO() {
    }

    public CcsRangeMatchesDTO(double value, double tolerance, double tolerancePercentage, List<CompoundCcsDTO> matches) {
        this.value = value;
        this.tolerance = tolerance;
        this.tolerancePercentage = tolerancePercentage;
        this.matches = matches;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    public double getTolerancePercentage() {
        return tolerancePercentage;
    }

    public void setTolerancePercentage(double tolerancePercentage) {
        this.tolerancePercentage = tolerancePercentage;
    }

    public List<CompoundCcsDTO> getMatches() {
        return matches;
    }

    public void setMatches(List<CompoundCcsDTO> matches) {
        this.matches = matches;
    }
}
