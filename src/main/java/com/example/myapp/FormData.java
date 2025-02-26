package com.example.myapp;

import java.util.List;

public class FormData {
    private String experimental_mass;
    private String tolerance;
    private String tolerance_type;
    private String metabolites;
    private String mass_mode;
    private String ionization_mode;
    private String adducts;
    private List<String> databases;

    public String getExperimental_mass() {
        return experimental_mass;
    }

    public void setExperimental_mass(String experimental_mass) {
        this.experimental_mass = experimental_mass;
    }

    public String getTolerance() {
        return tolerance;
    }

    public void setTolerance(String tolerance) {
        this.tolerance = tolerance;
    }

    public String getTolerance_type() {
        return tolerance_type;
    }

    public void setTolerance_type(String tolerance_type) {
        this.tolerance_type = tolerance_type;
    }

    public String getMetabolites() {
        return metabolites;
    }

    public void setMetabolites(String metabolites) {
        this.metabolites = metabolites;
    }

    public String getMass_mode() {
        return mass_mode;
    }

    public void setMass_mode(String mass_mode) {
        this.mass_mode = mass_mode;
    }

    public String getIonization_mode() {
        return ionization_mode;
    }

    public void setIonization_mode(String ionization_mode) {
        this.ionization_mode = ionization_mode;
    }

    public String getAdducts() {
        return adducts;
    }

    public void setAdducts(String adducts) {
        this.adducts = adducts;
    }

    public List<String> getDatabases() {
        return databases;
    }

    public void setDatabases(List<String> databases) {
        this.databases = databases;
    }
}