package ceu.biolab.cmm.unit.shared.service;

import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.service.MzToleranceConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MzToleranceConverterTest {

    @Test
    void toDaltons_convertsPpmUsingReferenceValue() {
        double da = MzToleranceConverter.toDaltons(MzToleranceMode.PPM, 10.0, 500.0);
        assertEquals(0.005, da, 1e-12);
    }

    @Test
    void toDaltons_convertsMdaToDa() {
        double da = MzToleranceConverter.toDaltons(MzToleranceMode.MDA, 100.0, 500.0);
        assertEquals(0.1, da, 1e-12);
    }

    @Test
    void toDaltons_throwsWhenModeIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> MzToleranceConverter.toDaltons(null, 10.0, 500.0));
        assertEquals("m/z tolerance mode is required", ex.getMessage());
    }
}
