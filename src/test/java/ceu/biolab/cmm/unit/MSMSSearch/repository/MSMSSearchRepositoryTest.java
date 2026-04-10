package ceu.biolab.cmm.unit.MSMSSearch.repository;

import ceu.biolab.cmm.MSMSSearch.domain.CIDEnergy;
import ceu.biolab.cmm.MSMSSearch.domain.MSMSAnnotation;
import ceu.biolab.cmm.MSMSSearch.domain.SpectrumSource;
import ceu.biolab.cmm.shared.domain.msFeature.ScoreType;
import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMSSearch.repository.MSMSSearchRepository;
import ceu.biolab.cmm.MSMSSearch.domain.Spectrum;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.shared.domain.compound.CompoundType;
import ceu.biolab.cmm.shared.domain.compound.Pathway;
import ceu.biolab.cmm.shared.domain.msFeature.MSPeak;
import ceu.biolab.cmm.shared.service.adduct.AdductService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MSMSSearchRepositoryTest {

    private Compound dummyCompound(int id) {
        return new Compound(
                id, "cas", "name", "H2O", 18.0,
                0, 0, FormulaType.CHNOPS, CompoundType.NON_LIPID,
                0.0, 0.0, "inchi", "inchikey", "smiles", "lipid",
                0, 0, 0, "bio", "mesh", "iupac", "mol2", new HashSet<Pathway>()
        );
    }

    @Test
    void selectBestPerCompound_picksHighestScore() {
        Compound cmp1 = dummyCompound(1);
        Compound cmp2 = dummyCompound(2);

        MSMSAnnotation a1 = new MSMSAnnotation();
        a1.setCompound(cmp1); a1.setMsmsCosineScore(0.6);

        MSMSAnnotation a2 = new MSMSAnnotation();
        a2.setCompound(cmp1); a2.setMsmsCosineScore(0.9);

        MSMSAnnotation b1 = new MSMSAnnotation();
        b1.setCompound(cmp2); b1.setMsmsCosineScore(0.7);

        Set<MSMSAnnotation> best = MSMSSearchRepository.selectBestPerCompound(List.of(a1, a2, b1));
        assertEquals(2, best.size());

        // Ensure best for compound 1 is 0.9
        double maxCmp1 = best.stream()
                .filter(m -> m.getCompound().getCompoundId() == 1)
                .mapToDouble(MSMSAnnotation::getMsmsCosineScore)
                .max().orElse(0);
        assertEquals(0.9, maxCmp1, 1e-9);
    }

    @Test
    void getMSMSWithScores_filtersByThreshold() throws Exception {
        // Build library spectra for two compounds
        MSMSAnnotation lib1 = new MSMSAnnotation();
        lib1.setCompound(dummyCompound(1));
        lib1.setSpectrum(new Spectrum(500.0, List.of(new MSPeak(100.0, 1.0))));

        MSMSAnnotation lib2 = new MSMSAnnotation();
        lib2.setCompound(dummyCompound(2));
        lib2.setSpectrum(new Spectrum(500.0, List.of(new MSPeak(200.0, 1.0))));

        List<MSMSAnnotation> libs = List.of(lib1, lib2);

        // Query has a peak matching lib1 only
        MSMSSearchRequestDTO query = new MSMSSearchRequestDTO();
        query.setPrecursorIonMZ(500.0);
        query.setFragmentsMZsIntensities(new Spectrum(500.0, new ArrayList<>(List.of(new MSPeak(100.0, 1.0)))));
        query.setScoreType(ScoreType.COSINE);
        query.setToleranceModePrecursorIon(MzToleranceMode.MDA);
        query.setToleranceFragments(100.0);

        MSMSSearchRepository repo = new MSMSSearchRepository(null, null);
        List<MSMSAnnotation> out = repo.getMSMSWithScores(ScoreType.COSINE, libs, query,
                query.getToleranceModePrecursorIon().toString(), query.getToleranceFragments());

        // Expect only compound 1 above threshold >= 0.5
        assertEquals(1, out.size());
        assertEquals(1, out.get(0).getCompound().getCompoundId());
        assertTrue(out.get(0).getMsmsCosineScore() >= 0.5);
    }

    @Test
    void getMSMSWithScores_modifiedCosineMatchesNeutralLoss() throws Exception {
        MSMSAnnotation lib = new MSMSAnnotation();
        lib.setCompound(dummyCompound(1));
        lib.setSpectrum(new Spectrum(480.0, List.of(new MSPeak(380.0, 1.0))));

        List<MSMSAnnotation> libs = List.of(lib);

        MSMSSearchRequestDTO query = new MSMSSearchRequestDTO();
        query.setPrecursorIonMZ(500.0);
        query.setFragmentsMZsIntensities(new Spectrum(null, new ArrayList<>(List.of(new MSPeak(400.0, 1.0)))));
        query.setScoreType(ScoreType.MODIFIED_COSINE);
        query.setToleranceModePrecursorIon(MzToleranceMode.MDA);
        query.setToleranceFragments(500.0); // 0.5 Da

        MSMSSearchRepository repo = new MSMSSearchRepository(null, null);
        List<MSMSAnnotation> out = repo.getMSMSWithScores(ScoreType.MODIFIED_COSINE, libs, query,
                query.getToleranceModePrecursorIon().toString(), query.getToleranceFragments());

        assertEquals(1, out.size(), "Modified cosine should recover neutral-loss matches");
        assertEquals(1, out.get(0).getCompound().getCompoundId());
        assertNotNull(out.get(0).getMsmsCosineScore());
        assertTrue(out.get(0).getMsmsCosineScore() > 0.9);
    }

    @Test
    void getMSMSWithScores_usesFragmentToleranceMode_notPrecursorMode() throws Exception {
        // Library has a peak at 100.000
        MSMSAnnotation lib = new MSMSAnnotation();
        lib.setCompound(dummyCompound(1));
        lib.setSpectrum(new Spectrum(500.0, List.of(new MSPeak(100.000, 1.0))));

        // Query peak is 100.004 -> 40 ppm at m/z 100
        MSMSSearchRequestDTO query = new MSMSSearchRequestDTO();
        query.setPrecursorIonMZ(500.0);
        query.setFragmentsMZsIntensities(new Spectrum(500.0, new ArrayList<>(List.of(new MSPeak(100.004, 1.0)))));
        query.setScoreType(ScoreType.COSINE);

        // Set conflicting tolerance modes: precursor=MDA, fragments=PPM (5 ppm)
        query.setToleranceModePrecursorIon(MzToleranceMode.MDA);
        query.setToleranceModeFragments(MzToleranceMode.PPM);
        query.setToleranceFragments(5.0); // 5 ppm should NOT match 40 ppm difference

        MSMSSearchRepository repo = new MSMSSearchRepository(null, null);
        List<MSMSAnnotation> out = repo.getMSMSWithScores(ScoreType.COSINE, List.of(lib), query,
                query.getToleranceModeFragments().toString(), query.getToleranceFragments());

        // Correct behavior: no match because we respect fragment PPM tolerance
        assertEquals(0, out.size());
    }

    @Test
    void getMsmsForCompound_skipsVoltageFilterWhenEnergyIsAll() throws Exception {
        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        Resource sqlResource = new ByteArrayResource("""
                SELECT msms_id, voltage AS ionization_voltage, predicted
                FROM msms
                WHERE compound_id = (:compound_id)
                  AND ionization_mode = (:ionization_mode)
                  (:voltage_filter_clause)
                  (:spectrum_source_filter_clause)
                """.getBytes(StandardCharsets.UTF_8));

        when(resourceLoader.getResource("classpath:sql/MSMS/MSMSSearch.sql")).thenReturn(sqlResource);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(List.of());

        MSMSSearchRepository repository = new MSMSSearchRepository(jdbcTemplate, resourceLoader);
        repository.getMsmsForCompound(
                dummyCompound(42),
                IonizationMode.POSITIVE,
                CIDEnergy.ALL,
                AdductService.requireDefinition(IonizationMode.POSITIVE, "[M+H]+"),
                100.0,
                SpectrumSource.ALL
        );

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), any(RowMapper.class));
        assertFalse(sqlCaptor.getValue().contains("voltage_level"));
        assertFalse(sqlCaptor.getValue().contains("predicted ="));
    }

    @Test
    void getMsmsForCompound_appliesVoltageFilterWhenEnergyIsSpecific() throws Exception {
        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        Resource sqlResource = new ByteArrayResource("""
                SELECT msms_id, voltage AS ionization_voltage, predicted
                FROM msms
                WHERE compound_id = (:compound_id)
                  AND ionization_mode = (:ionization_mode)
                  (:voltage_filter_clause)
                  (:spectrum_source_filter_clause)
                """.getBytes(StandardCharsets.UTF_8));

        when(resourceLoader.getResource("classpath:sql/MSMS/MSMSSearch.sql")).thenReturn(sqlResource);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(List.of());

        MSMSSearchRepository repository = new MSMSSearchRepository(jdbcTemplate, resourceLoader);
        repository.getMsmsForCompound(
                dummyCompound(42),
                IonizationMode.POSITIVE,
                CIDEnergy.MED,
                AdductService.requireDefinition(IonizationMode.POSITIVE, "[M+H]+"),
                100.0,
                SpectrumSource.PREDICTED
        );

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(sqlCaptor.capture(), any(RowMapper.class));
        assertTrue(sqlCaptor.getValue().contains("AND voltage_level = 'med'"));
        assertTrue(sqlCaptor.getValue().contains("AND predicted = 1"));
    }
}
