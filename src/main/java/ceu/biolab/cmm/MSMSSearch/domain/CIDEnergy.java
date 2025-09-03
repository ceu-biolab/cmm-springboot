package ceu.biolab.cmm.MSMSSearch.domain;

public enum CIDEnergy {
    LOW(10),
    MED(20),
    HIGH(40);
    //TODO en vez de ventanas que seleccione de voltage lever el to string.

    private final int value;

    CIDEnergy(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static CIDEnergy fromValue(int value) {
        for (CIDEnergy e : values()) {
            if (e.value == value) return e;
        }
        throw new IllegalArgumentException("Invalid CIDEnergy: " + value);
    }

    @Override
    public String toString() {
        String str = "";
        if (this == LOW) str= "low";
        if (this == MED) str= "med";
        if (this == HIGH) str= "high";
        return str;
    }
}
