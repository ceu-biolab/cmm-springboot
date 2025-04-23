package ceu.biolab.cmm.rtSearch.service;

import ceu.biolab.cmm.rtSearch.service.CompoundService;

import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.adduct.AdductTransformer;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.msFeature.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SimpleSearchServiceTest {
    @Autowired
    private CompoundService compoundService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testRangeWith1Da() {
        double mz = 500.0;
        String adduct = "M+H";
        Set<String> adductsString = new HashSet<>();
        adductsString.add(adduct);
        IonizationMode ionizationMode = IonizationMode.POSITIVE;
        assertMassRange(mz, adductsString, ionizationMode, 1.0, MzToleranceMode.MDA);
    }

    @Test
    void testRangeWith10Da() {
        double mz = 500.0;
        String adduct = "M+H";
        Set<String> adductsString = new HashSet<>();
        adductsString.add(adduct);
        IonizationMode ionizationMode = IonizationMode.POSITIVE;
        assertMassRange(mz, adductsString, ionizationMode, 10.0, MzToleranceMode.MDA);
    }

    @Test
    void testRangeWith100Da() {
        double mz = 500.0;
        String adduct = "M+H";
        Set<String> adductsString = new HashSet<>();
        adductsString.add(adduct);
        IonizationMode ionizationMode = IonizationMode.POSITIVE;
        assertMassRange(mz, adductsString, ionizationMode, 100.0, MzToleranceMode.MDA);
    }

    @Test
    void testRangeWith100PPM() {
        double mz = 500.0;
        String adduct = "M+H";
        Set<String> adductsString = new HashSet<>();
        adductsString.add(adduct);
        IonizationMode ionizationMode = IonizationMode.POSITIVE;
        assertMassRange(mz, adductsString, ionizationMode, 100.0, MzToleranceMode.PPM);
    }

    @Test
    void testRangeWith1000PPM() {
        double mz = 500.0;
        String adduct = "M+H";
        Set<String> adductsString = new HashSet<>();
        adductsString.add(adduct);
        IonizationMode ionizationMode = IonizationMode.POSITIVE;
        assertMassRange(mz, adductsString, ionizationMode, 1000.0, MzToleranceMode.PPM);
    }

    private void assertMassRange(double mz, Set<String> adduct, IonizationMode ionMode, double tolerance, MzToleranceMode mode) {
        for(String adductString : adduct) {
            double monoMass = AdductTransformer.getMonoisotopicMassFromMZ(mz, adductString, ionMode);
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

    private AnnotatedFeature createEmptyAnnotatedFeature(double mz) {
        AnnotatedFeature feature = new AnnotatedFeature(mz);
        feature.setFeature(new MSFeature(mz));
        return feature;
    }

    // Extract all compounds from a list of annotated features
    private List<Annotation> extractAnnotations(List<AnnotatedFeature> features) {
        List<Annotation> compounds = new ArrayList<>();
        for (AnnotatedFeature feature : features) {
            for (AnnotationsByAdduct commpoundsAnnotatedByAdduct : feature.getAnnotationsByAdducts()) {
                compounds.addAll(commpoundsAnnotatedByAdduct.getAnnotations());
            }
        }
        return compounds;
    }
}

//TODO test about methods of AdductTransformer
