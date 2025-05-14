package ceu.biolab.cmm.unit.shared.domain.adduct;

import ceu.biolab.cmm.shared.domain.Constants;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.adduct.AdductList;
import ceu.biolab.cmm.shared.service.adduct.AdductProcessing;
import org.drools.core.phreak.EagerPhreakBuilder;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.assertj.core.api.FactoryBasedNavigableListAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

public class AdductProcessingTest {

    @Test
    public void testFilterIsotopes() {
        Map<Double, Double> inputPeaks = new TreeMap<>();
        inputPeaks.put(400.3423, 307034.88);
        inputPeaks.put(401.34576, 73205.016);
        inputPeaks.put(402.3504, 15871.166);
        inputPeaks.put(403.35446, 2379.5325);
        inputPeaks.put(404.3498, 525.92053);

        Map<Double, Double> filteredPeaks = AdductProcessing.filterIsotopes(inputPeaks);

        assertTrue(filteredPeaks.containsKey(400.3423), "Peak 400.3432 should be present");
        assertFalse(filteredPeaks.containsKey(403.35446), "Peak 403.35446 should be present");
        assertFalse(filteredPeaks.containsKey(404.3498), "Peak 404.3498 should be present");
    }

    @Test
    public void testGetAdductMapByIonizationMode() {
        Map<String, String> ionizationMAPTest = new HashMap<>();
        Map<String, String> positiveMap = AdductList.MAPMZPOSITIVEADDUCTS;

        ionizationMAPTest = AdductProcessing.getAdductMapByIonizationMode(IonizationMode.POSITIVE);
        assertEquals(positiveMap, ionizationMAPTest);
    }


    @Test
    public void testDetectAdduct() {
        String detectedAdduct = "M+H";

        Double mz = 316.24945d;
        Set<String> adducts = new HashSet<>();
        String adduct1 = "M+H";
        String adduct2 = "M+2H";
        String adduct3 = "M+Na";
        String adduct4 = "M+K";
        String adduct5 = "M+NH4";
        String adduct6 = "M+H-H20";
        adducts.add(adduct1);
        adducts.add(adduct2);
        adducts.add(adduct3);
        adducts.add(adduct4);
        adducts.add(adduct5);
        adducts.add(adduct6);

        Map<Double, Double> peaks = new HashMap<>();
        peaks.put(631.4875, 367.90726);
        peaks.put(632.4899, 261.73);
        peaks.put(316.24945, 569921.25);
        peaks.put(317.2518, 100396.53);
        peaks.put(318.25354, 13153.248);
        peaks.put(319.2558, 1834.3552);
        peaks.put(320.25305, 241.56665);
        peaks.put(338.2299, 1832.6085);
        peaks.put(339.2322, 468.8131);

        String detectedAdductTest = AdductProcessing.detectAdductBasedOnCompositeSpectrum(IonizationMode.POSITIVE, mz, adducts, peaks);
        assertEquals(detectedAdduct, detectedAdductTest);
    }


    /**
     * Test of getMassToSearch method, of class AdductProcessing.
     */
    @Test
    public void testGetMassToSearch() {
        System.out.println("getMassToSearch");
        //System.out.println("exp: " + expResult + " res: " + result);
        // M+H adduct
        Double inputMass = 400.3432;
        String adduct = "M+H";
        Double adductValue = -1.007276d;
        Double expResult = 399.3359;
        Double result = AdductProcessing.getMassToSearch(inputMass, adduct, adductValue);
        assertEquals(expResult, result, 0.001);
        // M+H+2K adduct
        adduct = "M+H+2K";
        adductValue = -26.3112d;
        expResult = 1122.096;
        result = AdductProcessing.getMassToSearch(inputMass, adduct, adductValue);

        assertEquals(expResult, result, 0.001);
        // 2M+ACN+H adduct
        adduct = "2M+ACN+H";
        adductValue = -42.033823d;
        expResult = 179.154d;
        result = AdductProcessing.getMassToSearch(inputMass, adduct, adductValue);
        assertEquals(expResult, result, 0.001);
        // M+Cl adduct
        adduct = "M+Cl";
        adductValue = -34.969402d;
        expResult = 365.3738;
        result = AdductProcessing.getMassToSearch(inputMass, adduct, adductValue);
        assertEquals(expResult, result, 0.001);
        // 2M+HCOOH-H adduct
        adduct = "2M+HCOOH-H";
        adductValue = -44.998201d;
        expResult = 177.6725d;
        result = AdductProcessing.getMassToSearch(inputMass, adduct, adductValue);
        assertEquals(expResult, result, 0.001);
        // M-3H adduct
        adduct = "M+3H";
        adductValue = 1.007276d;
        expResult = 1204.05143;
        result = AdductProcessing.getMassToSearch(inputMass, adduct, adductValue);
        assertEquals(expResult, result, 0.001);
    }

    /**
     * Test of getAdductMass method, of class AdductProcessing.
     */


    /**
     * Test of detectAdductBasedOnCompositeSpectrum method, of class
     * AdductProcessing.
     */
    @Test
    public void testDetectAdductBasedOnCompositeSpectrum() {
        String massesMode = "mz";
        IonizationMode ionizationMode = IonizationMode.POSITIVE;
        Double inputMass = 281.24765d;
        Set<String> adducts = new HashSet<>();
        String adduct1 = "M+H";
        String adduct2 = "M+2H";
        String adduct3 = "M+Na";
        String adduct4 = "M+K";
        String adduct5 = "M+NH4";
        String adduct6 = "M+H-H20";
        adducts.add(adduct1);
        adducts.add(adduct2);
        adducts.add(adduct3);
        adducts.add(adduct4);
        adducts.add(adduct5);
        adducts.add(adduct6);

        Map<String, String> provisionalMap = AdductProcessing.getAdductMapByIonizationMode(ionizationMode);

        Map<Double, Double> compositeSpectrum = new TreeMap();

        compositeSpectrum.put(561.4858d, 236d);
        compositeSpectrum.put(141.1306, 297d);
        compositeSpectrum.put(281.24765, 8532d);
        compositeSpectrum.put(263.23685, 2734d);
        compositeSpectrum.put(264.24228, 616d);
        compositeSpectrum.put(265.2474, 97d);
        compositeSpectrum.put(303.2296, 3154d);
        compositeSpectrum.put(304.2393, 718d);
        compositeSpectrum.put(305.23438, 272d);
        String expResult = "M+H";
        String result;
        result = AdductProcessing.detectAdductBasedOnCompositeSpectrum(ionizationMode, inputMass, adducts, compositeSpectrum);
        assertEquals(expResult, result);

        // positive M+H-H2O
        massesMode = "mz";
        inputMass = 265.25244d;
        provisionalMap = AdductProcessing.getAdductMapByIonizationMode(ionizationMode);

        compositeSpectrum = new TreeMap();

        compositeSpectrum.put(265.25244, 2643d);
        compositeSpectrum.put(266.2552, 546d);
        compositeSpectrum.put(305.24606, 811d);
        compositeSpectrum.put(306.2479, 286d);

        expResult = "M+H-H2O";
        result = AdductProcessing.detectAdductBasedOnCompositeSpectrum(ionizationMode, inputMass, adducts, compositeSpectrum);
        assertEquals(expResult, result);

    }

    @Test
    public void testDetectionOfAdductH() {
        String massesMode = "mz";
        IonizationMode ionizationMode = IonizationMode.POSITIVE;
        Map<String, String> provisionalMap = AdductProcessing.getAdductMapByIonizationMode(ionizationMode);
        double inputMass = 281.24765d;

        Set<String> adducts = new HashSet<>();
        String adduct1 = "M+H";
        String adduct2 = "M+2H";
        String adduct3 = "M+Na";
        String adduct4 = "M+K";
        String adduct5 = "M+NH4";
        String adduct6 = "M+H-H20";
        adducts.add(adduct1);
        adducts.add(adduct2);
        adducts.add(adduct3);
        adducts.add(adduct4);
        adducts.add(adduct5);
        adducts.add(adduct6);

        Map<Double, Double> compositeSpectrum = new TreeMap();
        compositeSpectrum.put(561.4858d, 0d);
        compositeSpectrum.put(563.4868d, 0d);
        compositeSpectrum.put(141.1306d, 0d);
        compositeSpectrum.put(281.24765d, 0d);
        compositeSpectrum.put(263.23685d, 0d);
        compositeSpectrum.put(264.24228d, 0d);
        compositeSpectrum.put(265.2474d, 0d);
        compositeSpectrum.put(303.2296d, 0d);
        compositeSpectrum.put(304.2393d, 0d);
        compositeSpectrum.put(305.23438d, 0d);
        String expResult = "M+H";
        String result;
        result = AdductProcessing.detectAdductBasedOnCompositeSpectrum(ionizationMode, inputMass, adducts, compositeSpectrum);
        assertEquals(expResult, result);

    }

    @Test
    public void testDetectionOfAdductHH2O() {
        String massesMode = "mz";
        IonizationMode ionizationMode = IonizationMode.POSITIVE;
        double inputMass = 265.25244d;
        Map<String, String> provisionalMap = AdductProcessing.getAdductMapByIonizationMode(ionizationMode);

        Set<String> adducts = new HashSet<>();
        String adduct1 = "M+H";
        String adduct2 = "M+2H";
        String adduct3 = "M+Na";
        String adduct4 = "M+K";
        String adduct5 = "M+NH4";
        String adduct6 = "M+H-H20";
        adducts.add(adduct1);
        adducts.add(adduct2);
        adducts.add(adduct3);
        adducts.add(adduct4);
        adducts.add(adduct5);
        adducts.add(adduct6);

        Map<Double, Double> compositeSpectrum = new TreeMap();
        compositeSpectrum.put(265.25244d, 0d);
        compositeSpectrum.put(266.2552d, 0d);
        compositeSpectrum.put(305.24606d, 0d);
        compositeSpectrum.put(306.2479d, 0d);
        String expResult = "M+H-H2O";
        String result;
        result = AdductProcessing.detectAdductBasedOnCompositeSpectrum(ionizationMode, inputMass, adducts, compositeSpectrum);
        assertEquals(expResult, result);
    }


    @Test
    public void testNotDetectAdductBasedOnCompositeSpectrum() {
        Double inputMass = 400.3432;
        IonizationMode ionizationMode = IonizationMode.POSITIVE;
        Map<String, String> provisionalMap = AdductProcessing.getAdductMapByIonizationMode(ionizationMode);

        Set<String> adducts = new HashSet<>();
        String adduct1 = "M+H";
        String adduct2 = "M+2H";
        String adduct3 = "M+Na";
        String adduct4 = "M+K";
        String adduct5 = "M+NH4";
        String adduct6 = "M+H-H20";
        adducts.add(adduct1);
        adducts.add(adduct2);
        adducts.add(adduct3);
        adducts.add(adduct4);
        adducts.add(adduct5);
        adducts.add(adduct6);

        Map<Double, Double> compositeSpectrum = new TreeMap();
        compositeSpectrum.put(400.3432d, 307034.88);
        compositeSpectrum.put(401.34576, 73205.016);
        compositeSpectrum.put(402.3504, 15871.166);
        compositeSpectrum.put(403.35446, 2379.5325);
        compositeSpectrum.put(404.3498, 525.92053);
        String expResult = "";
        String result;
        result = AdductProcessing.detectAdductBasedOnCompositeSpectrum(ionizationMode, inputMass, adducts, compositeSpectrum);
        assertEquals(expResult, result);
    }


    @Test
    void testGetChargeOfAdduct() {
        assertEquals(1, AdductProcessing.getChargeOfAdduct("M+Na", IonizationMode.POSITIVE), "1 charge");
        assertEquals(2, AdductProcessing.getChargeOfAdduct("M+2H", IonizationMode.POSITIVE), "2 charges");
        assertEquals(3, AdductProcessing.getChargeOfAdduct("M+3H", IonizationMode.POSITIVE), "3 charges");
    }


    @Test
    void testFormatAdductString() {
        assertEquals("[M+2H]2+", AdductProcessing.formatAdductString("M+2H", IonizationMode.POSITIVE));
        assertEquals("[M-H]-", AdductProcessing.formatAdductString("M-H", IonizationMode.NEGATIVE));
    }

    @Test
    void testGetDimmerOriginalMass() {
        double result = AdductProcessing.getDimmerOriginalMass(500.0, 10.0, 2);
        assertEquals(255.0, result, 0.0001);
    }

    @Test
    void testGetChargedAdductMass() {
        double result = AdductProcessing.getChargedAdductMass(300.0, 5.0, 2);
        assertEquals(145.0, result, 0.0001);
    }

    @Test
    void testGetChargedOriginalMass() {
        double result = AdductProcessing.getChargedOriginalMass(150.0, 5.0, 2);
        assertEquals(310.0, result, 0.0001);
    }

    @Test
    void testGetMassOfAdductFromMonoWeight() {
        assertEquals(251.007, AdductProcessing.getMassOfAdductFromMonoWeight(500.0, "M+2H", IonizationMode.POSITIVE), 0.001);
        assertEquals(167.674, AdductProcessing.getMassOfAdductFromMonoWeight(500.0, "M+3H", IonizationMode.POSITIVE), 0.001);
        assertEquals(522.99, AdductProcessing.getMassOfAdductFromMonoWeight(500.0, "M+Na", IonizationMode.POSITIVE), 0.001);
    }

    @Test
    void testGetMZFromSingleChargedMonoMass() {
        assertEquals(502.0, AdductProcessing.getMZFromSingleChargedMonoMass(500.0, 2.0), 0.0001);
        assertEquals(498.0, AdductProcessing.getMZFromSingleChargedMonoMass(500.0, -2.0), 0.0001);
        assertEquals(500.0, AdductProcessing.getMZFromSingleChargedMonoMass(500.0, 0.0), 0.0001);
    }

    @Test
    void testGetMZFromMultiChargedMonoMass() {
        assertEquals(251.0, AdductProcessing.getMZFromMultiChargedMonoMass(500.0, 1.0, 2), 0.0001);
        assertEquals(168.1667, AdductProcessing.getMZFromMultiChargedMonoMass(500.0, 1.5, 3), 0.0001);
        assertEquals(125.0, AdductProcessing.getMZFromMultiChargedMonoMass(500.0, 0.0, 4), 0.0001);
        assertEquals(500.0, AdductProcessing.getMZFromMultiChargedMonoMass(500.0, 0.0, 1), 0.0001);
    }

    @Test
    void testGetMZFromMultimerMonoMass() {
        assertEquals(1002.0, AdductProcessing.getMZFromMultimerMonoMass(500.0, 2.0, 2), 0.0001);
        assertEquals(1503.0, AdductProcessing.getMZFromMultimerMonoMass(500.0, 3.0, 3), 0.0001);
        assertEquals(2000.0, AdductProcessing.getMZFromMultimerMonoMass(500.0, 0.0, 4), 0.0001);
    }

}