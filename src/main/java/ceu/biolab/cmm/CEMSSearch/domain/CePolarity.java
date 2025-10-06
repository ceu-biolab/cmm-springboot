package ceu.biolab.cmm.CEMSSearch.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CePolarity {
    DIRECT(1, "Direct"),
    REVERSE(2, "Reverse");

    private final int databaseValue;
    private final String label;

    CePolarity(int databaseValue, String label) {
        this.databaseValue = databaseValue;
        this.label = label;
    }

    public int getDatabaseValue() {
        return databaseValue;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static CePolarity fromValue(String value) {
        for (CePolarity polarity : values()) {
            if (polarity.label.equalsIgnoreCase(value)) {
                return polarity;
            }
        }
        throw new IllegalArgumentException("Unknown CE polarity: " + value);
    }

    public static CePolarity fromDatabaseValue(int value) {
        for (CePolarity polarity : values()) {
            if (polarity.databaseValue == value) {
                return polarity;
            }
        }
        throw new IllegalArgumentException("Unknown CE polarity database value: " + value);
    }
}
