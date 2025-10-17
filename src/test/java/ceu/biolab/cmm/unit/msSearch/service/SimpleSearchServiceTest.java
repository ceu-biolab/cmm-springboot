package ceu.biolab.cmm.unit.msSearch.service;

import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.service.adduct.AdductService;
import ceu.biolab.cmm.shared.domain.adduct.AdductDefinition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SimpleSearchServiceTest {
    @BeforeEach
    void setUp() {
    }

    @Test
    void testRangeWith1Da() {
        double mz = 500.0;
        String adduct = "[M+H]+";
        Set<String> adductsString = new HashSet<>();
        adductsString.add(adduct);
        IonizationMode ionizationMode = IonizationMode.POSITIVE;
        assertMassRange(mz, adductsString, ionizationMode, 1.0, MzToleranceMode.MDA);
    }

    @Test
    void testRangeWith10Da() {
        double mz = 500.0;
        String adduct = "[M+H]+";
        Set<String> adductsString = new HashSet<>();
        adductsString.add(adduct);
        IonizationMode ionizationMode = IonizationMode.POSITIVE;
        assertMassRange(mz, adductsString, ionizationMode, 10.0, MzToleranceMode.MDA);
    }

    @Test
    void testRangeWith100Da() {
        double mz = 500.0;
        String adduct = "[M+H]+";
        Set<String> adductsString = new HashSet<>();
        adductsString.add(adduct);
        IonizationMode ionizationMode = IonizationMode.POSITIVE;
        assertMassRange(mz, adductsString, ionizationMode, 100.0, MzToleranceMode.MDA);
    }

    @Test
    void testRangeWith100PPM() {
        double mz = 500.0;
        String adduct = "[M+H]+";
        Set<String> adductsString = new HashSet<>();
        adductsString.add(adduct);
        IonizationMode ionizationMode = IonizationMode.POSITIVE;
        assertMassRange(mz, adductsString, ionizationMode, 100.0, MzToleranceMode.PPM);
    }

    @Test
    void testRangeWith1000PPM() {
        double mz = 500.0;
        String adduct = "[M+H]+";
        Set<String> adductsString = new HashSet<>();
        adductsString.add(adduct);
        IonizationMode ionizationMode = IonizationMode.POSITIVE;
        assertMassRange(mz, adductsString, ionizationMode, 1000.0, MzToleranceMode.PPM);
    }

    private void assertMassRange(double mz, Set<String> adduct, IonizationMode ionMode, double tolerance, MzToleranceMode mode) {
        for(String adductString : adduct) {
            AdductDefinition definition = AdductService.requireDefinition(ionMode, adductString);
            double monoMass = AdductService.neutralMassFromMz(mz, definition);
            double lowerBound;
            double upperBound;

            if (mode == MzToleranceMode.MDA) {
                lowerBound = monoMass - tolerance / 1000.0;
                upperBound = monoMass + tolerance / 1000.0;
            } else { // PPM
                double tolPPM = mz * tolerance / 1_000_000.0;
                lowerBound = monoMass - tolPPM;
                upperBound = monoMass + tolPPM;
            }

            System.out.printf("mz: %.4f, adduct: %s, mode: %s, tolerance: %.2f%n", mz, adduct, mode, tolerance);
            System.out.printf("Monoisotopic mass: %.6f%n", monoMass);
            System.out.printf("Range: %.6f - %.6f%n", lowerBound, upperBound);

            // Add actual assertions or logging here:
            assertTrue(lowerBound < monoMass);
            assertTrue(upperBound > monoMass);
            assertEquals(monoMass, (lowerBound + upperBound) / 2, 1e-6);
        }
    }
}
