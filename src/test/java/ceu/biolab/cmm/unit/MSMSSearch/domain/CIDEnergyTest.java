package ceu.biolab.cmm.unit.MSMSSearch.domain;

import ceu.biolab.cmm.MSMSSearch.domain.CIDEnergy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CIDEnergyTest {

    @Test
    void fromValue_validMappings() {
        assertEquals(CIDEnergy.LOW, CIDEnergy.fromValue(10));
        assertEquals(CIDEnergy.MED, CIDEnergy.fromValue(20));
        assertEquals(CIDEnergy.HIGH, CIDEnergy.fromValue(40));
    }

    @Test
    void fromValue_invalidThrows() {
        assertThrows(IllegalArgumentException.class, () -> CIDEnergy.fromValue(15));
    }

    @Test
    void toString_values() {
        assertEquals("low", CIDEnergy.LOW.toString());
        assertEquals("med", CIDEnergy.MED.toString());
        assertEquals("high", CIDEnergy.HIGH.toString());
    }
}

