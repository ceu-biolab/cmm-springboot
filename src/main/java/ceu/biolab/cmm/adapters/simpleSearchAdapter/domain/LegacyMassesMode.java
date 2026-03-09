package ceu.biolab.cmm.adapters.simpleSearchAdapter.domain;


public enum LegacyMassesMode {

    MZ("mz"), 
    NEUTRAL("neutral");

    private final String value;

    LegacyMassesMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

