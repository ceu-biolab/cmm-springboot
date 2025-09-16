package ceu.biolab.cmm.MSMSSearch.repository;

import ceu.biolab.cmm.MSMSSearch.domain.*;
import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchResponseDTO;
import ceu.biolab.cmm.msSearch.domain.compound.CompoundMapper;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.msFeature.MSPeak;
import ceu.biolab.cmm.shared.domain.msFeature.ScoreType;
import ceu.biolab.cmm.shared.service.SpectrumScorer;
import ceu.biolab.cmm.shared.service.adduct.AdductProcessing;
import ceu.biolab.cmm.shared.service.adduct.AdductTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Repository
public class MSMSSearchRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ResourceLoader resourceLoader;

    @Autowired
    public MSMSSearchRepository(NamedParameterJdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;

    }

    public MSMSSearchResponseDTO findMatchingCompoundsAndSpectra(MSMSSearchRequestDTO queryData) throws IOException {
        if (queryData == null) {
            throw new IllegalArgumentException("Search request cannot be null");
        }

        // Build query spectrum from JSON
        MSMSAnnotation queryMsms = new MSMSAnnotation();
        Spectrum querySpectrum = new Spectrum(queryData.getPrecursorIonMZ(), queryData.getFragmentsMZsIntensities().getPeaks());
        queryMsms.setSpectrum(querySpectrum);

        // Prepare response containers
        Set<MSMSAnnotation> matchedSpectra = new HashSet<>();
        MSMSSearchResponseDTO responseDTO = new MSMSSearchResponseDTO(new ArrayList<>());

        // Determine adduct map based on ionization mode
        Map<String, String> adductMap = AdductProcessing.getAdductMapByIonizationMode(queryData.getIonizationMode());

        // Load window search SQL
        Resource windowResource = resourceLoader.getResource("classpath:sql/MSMS/WindowSearch.sql");
        String windowSql = new String(Files.readAllBytes(Paths.get(windowResource.getURI())));

        // Iterate over each adduct
        for (String adduct : queryData.getAdducts()) {
            Set<Compound> compoundsSet = new HashSet<>();
            if (!adductMap.containsKey(adduct)) {
                throw new IllegalArgumentException("Adduct not found: " + adduct);
            }

            // Compute neutral mass and tolerance window
            double neutralMass = AdductTransformer.getMonoisotopicMassFromMZ(
                    queryData.getPrecursorIonMZ(), adduct, queryData.getIonizationMode()
            );
            double delta;
            if (queryData.getToleranceModePrecursorIon() == MzToleranceMode.PPM) {
                // ppm to Da at neutral mass
                delta = neutralMass * (queryData.getTolerancePrecursorIon() / 1_000_000.0);
            } else {
                // mDa to Da
                delta = queryData.getTolerancePrecursorIon() / 1000.0;
            }
            double lowerBound = neutralMass - delta ;
            double upperBound = neutralMass + delta ;

            // Replace bounds in SQL
            String sql = windowSql
                    .replace("(:lowerBound)", String.valueOf(lowerBound))
                    .replace("(:upperBound)", String.valueOf(upperBound));

            // Query compounds within mass window
            jdbcTemplate.query(sql, rs -> {
                while (rs.next()) {
                    Compound compound = CompoundMapper.toCompound(
                            CompoundMapper.fromResultSet(rs)
                    );
                    compoundsSet.add(compound);
                }
                return null;
            });

            Set<MSMSAnnotation> libSpectra= new HashSet<>();
            for (Compound compound : compoundsSet) {
                libSpectra.addAll(getSpectraForCompounds(compound, queryData.getIonizationMode(), queryData.getCIDEnergy(), adduct, querySpectrum.getPrecursorMz()));
            }
            matchedSpectra.addAll(
                    getMSMSWithScores(
                            queryData.getScoreType(),
                            new ArrayList<>(libSpectra),
                            queryData,
                            queryData.getToleranceModeFragments().toString(),
                            queryData.getToleranceFragments()
                    )
            );
        }

        matchedSpectra=selectBestPerCompound(new ArrayList<>(matchedSpectra));
        responseDTO.setMsmsList(new ArrayList<>(matchedSpectra));

        return responseDTO;
    }

    public List<MSMSAnnotation> getSpectraForCompounds(Compound compound, IonizationMode ionizationMode,
                                                      CIDEnergy voltageEnergy, String adduct, Double queryMz) throws IOException {
        Set<MSMSAnnotation> msmsSet = getMsmsForCompound(compound, ionizationMode, voltageEnergy, adduct, queryMz);
        List<MSMSAnnotation> spectra = new ArrayList<>();
        for (MSMSAnnotation msms : msmsSet) {
            // Fetch precursor m/z and peaks
            Spectrum spectrum = getPeaksForMsms(msms.getMsmsId(), msms.getSpectrum().getPrecursorMz());
            msms.setSpectrum(spectrum);
            spectra.add(msms);
        }
        return spectra;
    }

    public Set<MSMSAnnotation> getMsmsForCompound(Compound compound, IonizationMode ionMode,
                                                 CIDEnergy voltage, String adduct, Double queryMz) throws IOException {
        Resource rsrc = resourceLoader.getResource("classpath:sql/MSMS/MSMSSearch.sql");
        String sql = new String(Files.readAllBytes(Paths.get(rsrc.getURI())));
        sql = sql.replace("(:compound_id)", String.valueOf(compound.getCompoundId()));

        int mode = switch (ionMode) {
            case POSITIVE -> 1;
            case NEGATIVE -> 2;
            case NEUTRAL  -> 3;
        };
        sql = sql.replace("(:ionization_mode)", String.valueOf(mode))
                .replace("(:voltage_level)", voltage.toString());

        Set<MSMSAnnotation> msmsSet = new HashSet<>();
        jdbcTemplate.query(sql, (rs, rowNum) -> {
            MSMSAnnotation msms = new MSMSAnnotation();
            msms.setCompound(compound);
            msms.setMsmsId(rs.getInt("msms_id"));
            try {
                double ce = rs.getDouble("ionization_voltage");
                if (!rs.wasNull()) {
                    msms.setCollisionEnergy(ce);
                }
            } catch (Exception ignored) {
                // Column missing in some schemas; leave collisionEnergy null
            }

            // Compute theoretical precursor m/z for this compound with the requested adduct
            String formattedAdduct = AdductProcessing.formatAdductString(adduct, ionMode);
            Double libPrecursorMz = AdductTransformer.getMassOfAdductFromMonoMass(compound.getMass(), formattedAdduct, ionMode);
            Spectrum spectrum = new Spectrum(libPrecursorMz, new ArrayList<>());
            msms.setSpectrum(spectrum);

            // Signed ppm difference between query precursor and library precursor
            double deltaPPM = ((queryMz - libPrecursorMz) / libPrecursorMz) * 1_000_000.0;
            msms.setDeltaPpmPrecursorIon(deltaPPM);

            // Set adduct used
            msms.setAdduct(adduct);

            msmsSet.add(msms);
            return null;
        });
        return msmsSet;
    }

    public Spectrum getPeaksForMsms(int msmsId, Double precursorMz) throws IOException {
        Resource rsrc = resourceLoader.getResource("classpath:sql/MSMS/PeakSearch.sql");
        String sql = new String(Files.readAllBytes(Paths.get(rsrc.getURI())));
        sql = sql.replace("(:msmsId)", String.valueOf(msmsId));

        List<MSPeak> peaks = new ArrayList<>();
        jdbcTemplate.query(sql, (rs, rowNum) -> {
            double mz = rs.getDouble("mz");
            double intensity = rs.getDouble("intensity");
            MSPeak pk = new MSPeak(mz, intensity);
            peaks.add(pk);
            return null;
        });

        return new Spectrum(precursorMz, peaks);
    }

    public List<MSMSAnnotation> getMSMSWithScores(ScoreType scoreType, List<MSMSAnnotation> libraryMsms, MSMSSearchRequestDTO queryMsms, String queryTolMode, Double tolValue) throws IOException {
        SpectrumScorer comparator = new SpectrumScorer(MzToleranceMode.valueOf(queryTolMode), tolValue);
        Set<MSMSAnnotation> matched = new HashSet<>();
        for (MSMSAnnotation lib : libraryMsms) {
            double score = comparator.compute(scoreType, lib.getSpectrum().getPeaks(), queryMsms.getFragmentsMZsIntensities().getPeaks());
            if (score >= 0.5) {
                lib.setMsmsCosineScore(score);
                matched.add(lib);
            }
        }
        return new ArrayList<>(matched);
    }

    public static Set<MSMSAnnotation> selectBestPerCompound(List<MSMSAnnotation> allSpectra) {
        Map<String, MSMSAnnotation> best = new HashMap<>();
        for (MSMSAnnotation sp : allSpectra) {
            best.compute(String.valueOf(sp.getCompound().getCompoundId()), (id, currentBest) -> {
                if (currentBest == null || sp.getMsmsCosineScore() > currentBest.getMsmsCosineScore()) {
                    return sp;
                } else {
                    return currentBest;
                }
            });
        }
        return new HashSet<> (best.values());
    }

}
