package ceu.biolab.cmm.unit.shared.validation;

import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.validation.MzToleranceLimits;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MzToleranceLimitsTest {

    @Test
    void maxAllowed_returns100ForBothModes() {
        assertEquals(100.0d, MzToleranceLimits.maxAllowed(MzToleranceMode.PPM));
        assertEquals(100.0d, MzToleranceLimits.maxAllowed(MzToleranceMode.MDA));
    }

    @Test
    void exceedsLimit_appliesModeSpecificMax() {
        assertFalse(MzToleranceLimits.exceedsLimit(100.0d, MzToleranceMode.PPM));
        assertTrue(MzToleranceLimits.exceedsLimit(100.0001d, MzToleranceMode.PPM));
        assertFalse(MzToleranceLimits.exceedsLimit(100.0d, MzToleranceMode.MDA));
        assertTrue(MzToleranceLimits.exceedsLimit(100.0001d, MzToleranceMode.MDA));
    }
}
