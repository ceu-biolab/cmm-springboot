package ceu.biolab.cmm.CEMSSearch.domain;

import ceu.biolab.cmm.shared.domain.IonizationMode;

public final class CeIonizationModeMapper {

    private CeIonizationModeMapper() {
    }

    public static int toDatabaseValue(IonizationMode ionizationMode) {
        if (ionizationMode == null) {
            throw new IllegalArgumentException("Ionization mode cannot be null");
        }
        return switch (ionizationMode) {
            case NEUTRAL -> 0;
            case POSITIVE -> 1;
            case NEGATIVE -> 2;
        };
    }
}
