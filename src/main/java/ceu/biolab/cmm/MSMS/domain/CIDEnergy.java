package ceu.biolab.cmm.MSMS.domain;

public enum CIDEnergy {
    E10(10),
    E20(20),
    E40(40);

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
}
