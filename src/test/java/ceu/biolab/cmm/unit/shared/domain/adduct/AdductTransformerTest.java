package ceu.biolab.cmm.unit.shared.domain.adduct;

import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.service.adduct.AdductTransformer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AdductTransformerTest {

    public static final double ELECTRON_MONOISOTOPIC_MASS = 0.0005485794321631d;

    @Test
    void testMonoMassFromSingleChargedMz() {
        double mz = 501.0;
        String adduct = "M+H";
        IonizationMode mode = IonizationMode.POSITIVE;

        double monoMassCalculated = mz - 1.007276;

        double monoMass = AdductTransformer.getMonoisotopicMassFromMZ(mz, adduct, mode);
        assertEquals(monoMassCalculated, monoMass, 0.001);
    }

    @Test
    void testMonoMassFromDoubleChargedMz() {
        double mz = 751.0;
        String adduct = "M+2H";
        int charge = 2; //2 charges: [M+2H]2+
        IonizationMode mode = IonizationMode.POSITIVE;
        double monoMassCalculated = mz - 2.01565;
        double result = monoMassCalculated * charge;
        result = result + charge * ELECTRON_MONOISOTOPIC_MASS;

        double monoMass = AdductTransformer.getMonoisotopicMassFromMZ(mz, adduct, mode);
        assertEquals(result, monoMass, 0.0001);
    }

    @Test
    void testMonoMassFromMultimerMz() {
        double mz = 501.0;
        String adduct = "2M+H";
        IonizationMode mode = IonizationMode.POSITIVE;

        double expectedResult = mz - 1.00783;
        expectedResult/= 2;
        expectedResult = expectedResult + ELECTRON_MONOISOTOPIC_MASS;

        double monoMass = AdductTransformer.getMonoisotopicMassFromMZ(mz, adduct, mode);
        assertEquals(expectedResult, monoMass, 0.0001);
    }

    @Test
    void testMzFromMonoMassSingleCharge() {
        double monoIsotopicWeight = 500.0;
        String adduct = "[M+H]+";
        IonizationMode mode = IonizationMode.POSITIVE;

        double mzCalculated = monoIsotopicWeight + 1.007276;

        double mz = AdductTransformer.getMassOfAdductFromMonoMass(monoIsotopicWeight, adduct, mode);
        assertEquals(mzCalculated, mz, 0.0001);
    }

    @Test
    void testMzFromMonoMassMultiCharge() {
        double monoisotopicWeight = 500.0;
        String adduct = "[M+2H]2+";
        IonizationMode mode = IonizationMode.POSITIVE;

        double result = monoisotopicWeight;
        double massAdduct = (2*1.007276);

        result /= 2;
        result += massAdduct;

        double mz = AdductTransformer.getMassOfAdductFromMonoMass(monoisotopicWeight, adduct, mode);
        assertEquals(result, mz, 0.0001);
    }

    @Test
    void testMzFromMultimerMonoMass() {
        double monoIsotopicWeight = 250.0;
        String adduct = "[2M+H]+";
        IonizationMode mode = IonizationMode.POSITIVE;

        double expectedResult = monoIsotopicWeight * 2;
        expectedResult += 1.00783;
        expectedResult = expectedResult-ELECTRON_MONOISOTOPIC_MASS;

        double mz = AdductTransformer.getMassOfAdductFromMonoMass(monoIsotopicWeight, adduct, mode);
        assertEquals(expectedResult, mz, 0.0001);
    }

    @Test
    void testCalculatePPMIncrement() {
        double measured = 500.5;
        double theoretical = 500.0;
        int ppm = AdductTransformer.calculatePPMIncrement(measured, theoretical);
        assertEquals(1000, ppm);
    }

    @Test
    void testCalculatePercentageError() {
        double experimental = 105.0;
        double theoretical = 100.0;
        int error = AdductTransformer.calculatePercentageError(experimental, theoretical);
        assertEquals(5, error);
    }
}