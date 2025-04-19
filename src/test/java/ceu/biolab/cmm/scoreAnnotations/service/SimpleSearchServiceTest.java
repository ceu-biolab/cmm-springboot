package ceu.biolab.cmm.scoreAnnotations.service;

import ceu.biolab.cmm.rtSearch.service.CompoundService;

import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.msFeature.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.xmlcml.euclid.test.DoubleTestBase.assertEquals;

@SpringBootTest
public class SimpleSearchServiceTest {
    @Autowired
    private CompoundService compoundService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testFindCompoundsByMz10DA() {
        Double mz = 100.0;
        MzToleranceMode mzToleranceMode = MzToleranceMode.MDA;
        Double tolerance = 1.0;
        Set<String> adducts = new HashSet<>();
        String adductsString = "M+H";
        adducts.add(adductsString);
        IonizationMode ionizationMode = IonizationMode.POSITIVE;
        Set<Database> databases = Set.of(Database.HMDB, Database.LIPIDMAPS);
        MetaboliteType metaboliteType = MetaboliteType.ONLYLIPIDS;

        List<AnnotatedFeature> results = compoundService.findCompoundsByMz(
                mz, mzToleranceMode, tolerance, ionizationMode, adducts, databases, metaboliteType
        );

        assertNotNull(results);
    }

    @Test
    void testFindCompoundsByMz50DA() {
        Double mz = 100.0;
        MzToleranceMode mzToleranceMode = MzToleranceMode.MDA;
        Double tolerance = 1.0;
        Set<String> adducts = new HashSet<>();
        String adductsString = "M+H";
        adducts.add(adductsString);
        IonizationMode ionizationMode = IonizationMode.POSITIVE;
        Set<Database> databases = Set.of(Database.HMDB, Database.LIPIDMAPS);
        MetaboliteType metaboliteType = MetaboliteType.ONLYLIPIDS;

        List<AnnotatedFeature> results = compoundService.findCompoundsByMz(
                mz, mzToleranceMode, tolerance, ionizationMode, adducts, databases, metaboliteType
        );

        assertNotNull(results);
    }

    @Test
    void testFindCompoundsByMz100DA() {
        Double mz = 100.0;
        MzToleranceMode mzToleranceMode = MzToleranceMode.MDA;
        Double tolerance = 1.0;
        Set<String> adducts = new HashSet<>();
        String adductsString = "M+H";
        adducts.add(adductsString);
        IonizationMode ionizationMode = IonizationMode.POSITIVE;
        Set<Database> databases = Set.of(Database.HMDB, Database.LIPIDMAPS);
        MetaboliteType metaboliteType = MetaboliteType.ONLYLIPIDS;

        List<AnnotatedFeature> results = compoundService.findCompoundsByMz(
                mz, mzToleranceMode, tolerance, ionizationMode, adducts, databases, metaboliteType
        );

        assertNotNull(results);
    }

    @Test
    void testFindCompoundsByMzPPMTolerance() {
        // Arrange test inputs
        Double mz = 500.0;
        MzToleranceMode mzToleranceMode = MzToleranceMode.PPM;
        Double tolerance = 10.0;
        Set<String> adducts = new HashSet<>();
        String adductsString = "M+H";
        adducts.add(adductsString);
        IonizationMode ionizationMode = IonizationMode.POSITIVE;
        Set<Database> databases = Set.of(Database.HMDB, Database.LIPIDMAPS);
        MetaboliteType metaboliteType = MetaboliteType.ONLYLIPIDS;

        List<AnnotatedFeature> results = compoundService.findCompoundsByMz(
                mz, mzToleranceMode, tolerance, ionizationMode, adducts, databases, metaboliteType
        );

        assertNotNull(results);
    }
}
