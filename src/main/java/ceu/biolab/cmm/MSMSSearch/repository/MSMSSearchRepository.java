package ceu.biolab.cmm.MSMSSearch.repository;

import ceu.biolab.cmm.MSMSSearch.domain.CIDEnergy;
import ceu.biolab.cmm.MSMSSearch.domain.MSMSAnnotation;
import ceu.biolab.cmm.MSMSSearch.domain.Spectrum;
import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchResponseDTO;
import ceu.biolab.cmm.msSearch.domain.compound.CompoundMapper;
import ceu.biolab.cmm.msSearch.domain.compound.LipidMapsClassification;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.msFeature.MSPeak;
import ceu.biolab.cmm.shared.domain.msFeature.ScoreType;
import ceu.biolab.cmm.shared.service.SpectrumScorer;
import ceu.biolab.cmm.shared.domain.adduct.AdductDefinition;
import ceu.biolab.cmm.shared.service.adduct.AdductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        // Load window search SQL
        Resource windowResource = resourceLoader.getResource("classpath:sql/MSMS/WindowSearch.sql");
        String windowSql = loadSql(windowResource);

        List<AdductDefinition> adductDefinitions = queryData.getAdducts().stream()
                .map(adduct -> AdductService.requireDefinition(queryData.getIonizationMode(), adduct))
                .toList();

        // Iterate over each adduct
        for (AdductDefinition adductDefinition : adductDefinitions) {
            Set<Compound> compoundsSet = new HashSet<>();

            // Compute neutral mass and tolerance window
            double neutralMass = AdductService.neutralMassFromMz(queryData.getPrecursorIonMZ(), adductDefinition);
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
                    normalizeLipidMapsClassification(compound);
                    compoundsSet.add(compound);
                }
                return null;
            });

            Set<MSMSAnnotation> libSpectra= new HashSet<>();
            for (Compound compound : compoundsSet) {
                libSpectra.addAll(getSpectraForCompounds(compound, queryData.getIonizationMode(), queryData.getCIDEnergy(), adductDefinition, querySpectrum.getPrecursorMz()));
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
                                                      CIDEnergy voltageEnergy, AdductDefinition adduct, Double queryMz) throws IOException {
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
                                                 CIDEnergy voltage, AdductDefinition adduct, Double queryMz) throws IOException {
        Resource rsrc = resourceLoader.getResource("classpath:sql/MSMS/MSMSSearch.sql");
        String sql = loadSql(rsrc);
        sql = sql.replace("(:compound_id)", String.valueOf(compound.getCompoundId()));

        int mode = switch (ionMode) {
            case POSITIVE -> 1;
            case NEGATIVE -> 2;
            case NEUTRAL  -> 3;
        };
        sql = sql.replace("(:ionization_mode)", String.valueOf(mode))
                .replace("(:voltage_level)", voltage.toString());

        Set<MSMSAnnotation> msmsSet = new HashSet<>();
        jdbcTemplate.query(sql, (rs, _) -> {
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
            Double libPrecursorMz = AdductService.mzFromNeutralMass(compound.getMass(), adduct);
            Spectrum spectrum = new Spectrum(libPrecursorMz, new ArrayList<>());
            msms.setSpectrum(spectrum);

            // Signed ppm difference between query precursor and library precursor
            double deltaPPM = ((queryMz - libPrecursorMz) / libPrecursorMz) * 1_000_000.0;
            msms.setDeltaPpmPrecursorIon(deltaPPM);

            // Set adduct used
            msms.setAdduct(adduct.canonical());

            msmsSet.add(msms);
            return null;
        });
        return msmsSet;
    }

    public Spectrum getPeaksForMsms(int msmsId, Double precursorMz) throws IOException {
        Resource rsrc = resourceLoader.getResource("classpath:sql/MSMS/PeakSearch.sql");
        String sql = loadSql(rsrc);
        sql = sql.replace("(:msmsId)", String.valueOf(msmsId));

        List<MSPeak> peaks = new ArrayList<>();
        jdbcTemplate.query(sql, (rs, _) -> {
            double mz = rs.getDouble("mz");
            double intensity = rs.getDouble("intensity");
            if (intensity > 1.0) {
                intensity = intensity / 100.0;
            }
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
            Double libPrecursor = lib.getSpectrum() != null ? lib.getSpectrum().getPrecursorMz() : null;
            Double queryPrecursor = queryMsms.getFragmentsMZsIntensities() != null ? queryMsms.getFragmentsMZsIntensities().getPrecursorMz() : null;
            double score = comparator.compute(scoreType,
                    lib.getSpectrum().getPeaks(),
                    queryMsms.getFragmentsMZsIntensities().getPeaks(),
                    libPrecursor,
                    queryPrecursor);
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
            best.compute(String.valueOf(sp.getCompound().getCompoundId()), (_, currentBest) -> {
                if (currentBest == null || sp.getMsmsCosineScore() > currentBest.getMsmsCosineScore()) {
                    return sp;
                } else {
                    return currentBest;
                }
            });
        }
        return new HashSet<> (best.values());
    }

    private void normalizeLipidMapsClassification(Compound compound) {
        if (compound == null || compound.getLipidMapsClassifications() == null) {
            return;
        }
        for (LipidMapsClassification classification : compound.getLipidMapsClassifications()) {
            if (classification == null) {
                continue;
            }
            classification.setCategory(extractCode(classification.getCategory()));
            classification.setMainClass(extractCode(classification.getMainClass()));
            classification.setSubClass(extractCode(classification.getSubClass()));
            classification.setClassLevel4(extractCode(classification.getClassLevel4()));
        }
    }

    private String extractCode(String value) {
        if (value == null) {
            return "";
        }
        Matcher matcher = BRACKET_CODE_PATTERN.matcher(value);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return value;
    }

    private static final Pattern BRACKET_CODE_PATTERN = Pattern.compile(".*\\[(.+?)\\].*");

    private String loadSql(Resource resource) throws IOException {
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

}
