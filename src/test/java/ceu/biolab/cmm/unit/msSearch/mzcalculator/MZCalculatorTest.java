package ceu.biolab.cmm.unit.msSearch.mzcalculator;

import ceu.biolab.cmm.msSearch.mzcalculator.MZCalculator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MZCalculatorTest {

    @Test
    void testGetMZFromSingleChargedMonoMass() {
        assertEquals(502.0, MZCalculator.getMZFromSingleChargedMonoMass(500.0, 2.0), 0.0001);
        assertEquals(498.0, MZCalculator.getMZFromSingleChargedMonoMass(500.0, -2.0), 0.0001);
        assertEquals(500.0, MZCalculator.getMZFromSingleChargedMonoMass(500.0, 0.0), 0.0001);
    }

    @Test
    void testGetMZFromMultiChargedMonoMass() {
        assertEquals(251.0, MZCalculator.getMZFromMultiChargedMonoMass(500.0, 1.0, 2), 0.0001);
        assertEquals(168.1667, MZCalculator.getMZFromMultiChargedMonoMass(500.0, 1.5, 3), 0.0001);
        assertEquals(125.0, MZCalculator.getMZFromMultiChargedMonoMass(500.0, 0.0, 4), 0.0001);
        assertEquals(500.0, MZCalculator.getMZFromMultiChargedMonoMass(500.0, 0.0, 1), 0.0001);
    }

    @Test
    void testGetMZFromMultimerMonoMass() {
        assertEquals(1002.0, MZCalculator.getMZFromMultimerMonoMass(500.0, 2.0, 2), 0.0001);
        assertEquals(1503.0, MZCalculator.getMZFromMultimerMonoMass(500.0, 3.0, 3), 0.0001);
        assertEquals(2000.0, MZCalculator.getMZFromMultimerMonoMass(500.0, 0.0, 4), 0.0001);
    }
}
