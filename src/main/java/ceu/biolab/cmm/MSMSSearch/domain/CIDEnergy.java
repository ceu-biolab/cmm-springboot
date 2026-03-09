package ceu.biolab.cmm.MSMSSearch.domain;

public enum CIDEnergy {
    LOW(10),
    MED(20),
    HIGH(40),
    ALL(-1);

    private final int value;

    CIDEnergy(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static CIDEnergy fromValue(int value) {
        for (CIDEnergy e : values()) {
            if (e != ALL && e.value == value) return e;
        }
        throw new IllegalArgumentException("Invalid CIDEnergy: " + value);
    }

    @Override
    public String toString() {
        return switch (this) {
            case LOW -> "low";
            case MED -> "med";
            case HIGH -> "high";
            case ALL -> "all";
        };
    }
}
