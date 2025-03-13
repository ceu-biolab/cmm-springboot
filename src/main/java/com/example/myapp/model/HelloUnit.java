package com.example.myapp.model;

import java.util.HashSet;
import java.util.Set;

import org.drools.ruleunits.api.DataSource;
import org.drools.ruleunits.api.DataStore;
import org.drools.ruleunits.api.RuleUnitData;

public class HelloUnit implements RuleUnitData {

    private final DataStore<Hello> hellos;
    private final Set<String> controlSet = new HashSet<>();

    public HelloUnit() {
        this(DataSource.createStore());
    }

    public HelloUnit(DataStore<Hello> hellos) {
        this.hellos = hellos;
    }

    public DataStore<Hello> getHellos() {
        return hellos;
    }

    public Set<String> getControlSet() {
        return controlSet;
    }
}
