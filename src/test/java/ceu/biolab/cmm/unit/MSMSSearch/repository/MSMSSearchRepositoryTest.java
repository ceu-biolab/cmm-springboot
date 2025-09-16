package ceu.biolab.cmm.unit.MSMSSearch.repository;

import ceu.biolab.cmm.MSMSSearch.domain.MSMSAnnotation;
import ceu.biolab.cmm.MSMSSearch.domain.ScoreType;
import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMSSearch.repository.MSMSSearchRepository;
import ceu.biolab.cmm.MSMSSearch.domain.Spectrum;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.shared.domain.compound.Pathway;
import ceu.biolab.cmm.shared.domain.msFeature.MSPeak;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class MSMSSearchRepositoryTest {

    private Compound dummyCompound(int id) {
        return new Compound(
                id, "cas", "name", "H2O", 18.0,
                0, 0, FormulaType.CHNOPS, 0, 0, 0,
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
    void getMSMSWithScores_usesFragmentToleranceMode_notPrecursorMode() throws Exception {
        // Library has a peak at 100.000
        MSMSAnnotation lib = new MSMSAnnotation();
        lib.setCompound(dummyCompound(1));
        lib.setSpectrum(new Spectrum(500.0, List.of(new MSPeak(100.000, 1.0))));

        // Query peak is 100.004 -> 40 ppm at m/z 100
        MSMSSearchRequestDTO query = new MSMSSearchRequestDTO();
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
}
