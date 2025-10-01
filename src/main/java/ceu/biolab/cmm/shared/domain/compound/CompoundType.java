package ceu.biolab.cmm.shared.domain.compound;

import java.util.Arrays;

public enum CompoundType {
    NON_LIPID(0),
    LIPID(1);

    private final int dbValue;

    CompoundType(int dbValue) {
        this.dbValue = dbValue;
    }

    public int getDbValue() {
        return dbValue;
    }

    public static CompoundType fromDbValue(Integer value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(type -> type.dbValue == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown compound type value: " + value));
    }
}
