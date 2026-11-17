package ceu.biolab.cmm.MSMSSearch.repository;

import ceu.biolab.cmm.shared.repository.SqlQueryLoader;
import ceu.biolab.cmm.shared.repository.JdbcBatches;

import ceu.biolab.cmm.MSMSSearch.domain.CIDEnergy;
import ceu.biolab.cmm.MSMSSearch.domain.MSMSAnnotation;
import ceu.biolab.cmm.MSMSSearch.domain.Spectrum;
import ceu.biolab.cmm.MSMSSearch.domain.SpectrumSource;
import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchResponseDTO;
import ceu.biolab.cmm.msSearch.domain.compound.CompoundMapper;
import ceu.biolab.cmm.shared.domain.compound.LipidMapsClassification;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.msFeature.MSPeak;
import ceu.biolab.cmm.shared.domain.msFeature.ScoreType;
import ceu.biolab.cmm.shared.service.SpectrumScorer;
import ceu.biolab.cmm.shared.service.MzToleranceConverter;
import ceu.biolab.cmm.shared.domain.adduct.AdductDefinition;
import ceu.biolab.cmm.shared.service.adduct.AdductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
public class MSMSSearchRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlQueryLoader sqlQueries;

    @Autowired
    public MSMSSearchRepository(NamedParameterJdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.sqlQueries = new SqlQueryLoader(resourceLoader);

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
        String windowSql = sqlQueries.load("classpath:sql/MSMS/WindowSearch.sql");

        List<AdductDefinition> adductDefinitions = queryData.getAdducts().stream()
                .map(adduct -> AdductService.requireDefinition(queryData.getIonizationMode(), adduct))
                .toList();

        Map<AdductDefinition, Set<Compound>> candidates = new LinkedHashMap<>();
        Set<Integer> compoundIds = new LinkedHashSet<>();
        for (AdductDefinition definition : adductDefinitions) {
            double neutralMass = AdductService.neutralMassFromMz(queryData.getPrecursorIonMZ(), definition);
            double delta = MzToleranceConverter.toDaltons(queryData.getToleranceModePrecursorIon(),
                    queryData.getTolerancePrecursorIon(), neutralMass);
            String sql = windowSql.replace("(:lowerBound)", ":lowerBound")
                    .replace("(:upperBound)", ":upperBound");
            Set<Compound> compounds = new HashSet<>(jdbcTemplate.query(sql,
                    Map.of("lowerBound", neutralMass - delta, "upperBound", neutralMass + delta), (rs, rowNum) -> {
                        Compound compound = CompoundMapper.toCompound(CompoundMapper.fromResultSet(rs));
                        normalizeLipidMapsClassification(compound);
                        return compound;
                    }));
            candidates.put(definition, compounds);
            compounds.forEach(compound -> compoundIds.add(compound.getCompoundId()));
        }
        Map<Integer, List<LibrarySpectrum>> library = loadLibrary(compoundIds, queryData);
        for (var entry : candidates.entrySet()) {
            for (Compound compound : entry.getValue()) {
                for (LibrarySpectrum source : library.getOrDefault(compound.getCompoundId(), List.of())) {
                    MSMSAnnotation annotation = new MSMSAnnotation();
                    annotation.setCompound(compound);
                    annotation.setMsmsId(source.id());
                    annotation.setSpectrumSource(source.source());
                    annotation.setCollisionEnergy(source.energy());
                    annotation.setAdduct(entry.getKey().canonical());
                    double precursor = AdductService.mzFromNeutralMass(compound.getMass(), entry.getKey());
                    annotation.setDeltaPpmPrecursorIon((querySpectrum.getPrecursorMz() - precursor) / precursor * 1_000_000d);
                    List<MSPeak> peaks = source.peaks().stream()
                            .map(peak -> new MSPeak(peak.getMz(), peak.getIntensity())).toList();
                    annotation.setSpectrum(new Spectrum(precursor, new ArrayList<>(peaks)));
                    matchedSpectra.add(annotation);
                }
            }
        }
        matchedSpectra = new HashSet<>(getMSMSWithScores(queryData.getScoreType(), new ArrayList<>(matchedSpectra),
                queryData, queryData.getToleranceModeFragments().toString(), queryData.getToleranceFragments()));

        matchedSpectra = selectBestPerCompound(new ArrayList<>(matchedSpectra));
        responseDTO.setMsmsList(new ArrayList<>(matchedSpectra));
        responseDTO.setExperimentalSpectrum(copySpectrum(querySpectrum));
        return responseDTO;
    }

    private record LibrarySpectrum(int id, SpectrumSource source, Double energy, List<MSPeak> peaks) {}

    private Map<Integer, List<LibrarySpectrum>> loadLibrary(Set<Integer> compoundIds, MSMSSearchRequestDTO request) {
        Map<Integer, List<LibrarySpectrum>> byCompound = new HashMap<>();
        if (compoundIds.isEmpty()) return byCompound;
        String sql = """
                SELECT compound_id, msms_id, voltage AS ionization_voltage, predicted
                FROM msms WHERE compound_id IN (:ids) AND ionization_mode = :mode
                """;
        if (request.getCIDEnergy() != CIDEnergy.ALL) sql += " AND voltage_level = :voltage";
        if (request.getSpectrumSource() != null) sql += " " + request.getSpectrumSource().sqlFilterClause();
        var params = new org.springframework.jdbc.core.namedparam.MapSqlParameterSource()
                .addValue("ids", compoundIds)
                .addValue("mode", request.getIonizationMode() == IonizationMode.POSITIVE ? 1 : 2)
                .addValue("voltage", request.getCIDEnergy().toString());
        Map<Integer, List<MSPeak>> bySpectrum = new HashMap<>();
        for (var batch : JdbcBatches.partition(compoundIds)) {
            params.addValue("ids", batch);
            jdbcTemplate.query(sql, params, (org.springframework.jdbc.core.RowCallbackHandler) rs -> {
                int id = rs.getInt("msms_id");
                long predicted = rs.getLong("predicted");
                SpectrumSource source = rs.wasNull() ? SpectrumSource.EXPERIMENTAL : SpectrumSource.fromPredictedFlag(predicted);
                double energy = rs.getDouble("ionization_voltage");
                Double nullableEnergy = rs.wasNull() ? null : energy;
                List<MSPeak> peaks = new ArrayList<>();
                bySpectrum.put(id, peaks);
                byCompound.computeIfAbsent(rs.getInt("compound_id"), ignored -> new ArrayList<>())
                        .add(new LibrarySpectrum(id, source, nullableEnergy, peaks));
            });
        }
        if (bySpectrum.isEmpty()) return byCompound;
        for (var batch : JdbcBatches.partition(bySpectrum.keySet()))
            jdbcTemplate.query("SELECT msms_id, mz, intensity FROM msms_peaks WHERE msms_id IN (:ids)",
                Map.of("ids", batch), (org.springframework.jdbc.core.RowCallbackHandler) rs ->
                        bySpectrum.get(rs.getInt("msms_id")).add(new MSPeak(rs.getDouble("mz"), rs.getDouble("intensity"))));
        bySpectrum.values().forEach(MSMSSearchRepository::normalizeLibraryPeaks);
        return byCompound;
    }

    private static void normalizeLibraryPeaks(List<MSPeak> peaks) {
        double maximum = peaks.stream().mapToDouble(MSPeak::getIntensity).max().orElse(0d);
        if (maximum > 0d) peaks.forEach(peak -> peak.setIntensity(peak.getIntensity() / maximum));
    }

    public List<MSMSAnnotation> getSpectraForCompounds(Compound compound, IonizationMode ionizationMode,
                                                      CIDEnergy voltageEnergy,
                                                      AdductDefinition adduct,
                                                      Double queryMz,
                                                      SpectrumSource spectrumSource) throws IOException {
        Set<MSMSAnnotation> msmsSet = getMsmsForCompound(compound, ionizationMode, voltageEnergy, adduct, queryMz, spectrumSource);
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
                                                 CIDEnergy voltage,
                                                 AdductDefinition adduct,
                                                 Double queryMz,
                                                 SpectrumSource spectrumSource) throws IOException {
        String sql = sqlQueries.load("classpath:sql/MSMS/MSMSSearch.sql");
        sql = sql.replace("(:compound_id)", String.valueOf(compound.getCompoundId()));

        int mode = switch (ionMode) {
            case POSITIVE -> 1;
            case NEGATIVE -> 2;
            case NEUTRAL  -> 3;
        };
        String voltageFilterClause = voltage == CIDEnergy.ALL
                ? ""
                : "AND voltage_level = '" + voltage + "'";
        String sourceFilterClause = spectrumSource == null ? "" : spectrumSource.sqlFilterClause();
        sql = sql.replace("(:ionization_mode)", String.valueOf(mode))
                .replace("(:voltage_filter_clause)", voltageFilterClause)
                .replace("(:spectrum_source_filter_clause)", sourceFilterClause);

        Set<MSMSAnnotation> msmsSet = new HashSet<>();
        jdbcTemplate.query(sql, (rs, _) -> {
            MSMSAnnotation msms = new MSMSAnnotation();
            msms.setCompound(compound);
            msms.setMsmsId(rs.getInt("msms_id"));
            long predictedFlag = rs.getLong("predicted");
            msms.setSpectrumSource(rs.wasNull()
                    ? SpectrumSource.EXPERIMENTAL
                    : SpectrumSource.fromPredictedFlag(predictedFlag));
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
        String sql = sqlQueries.load("classpath:sql/MSMS/PeakSearch.sql");
        sql = sql.replace("(:msmsId)", String.valueOf(msmsId));

        List<MSPeak> peaks = new ArrayList<>();
        jdbcTemplate.query(sql, (rs, _) -> {
            double mz = rs.getDouble("mz");
            double intensity = rs.getDouble("intensity");
            MSPeak pk = new MSPeak(mz, intensity);
            peaks.add(pk);
            return null;
        });

        normalizeLibraryPeaks(peaks);
        return new Spectrum(precursorMz, peaks);
    }

    public List<MSMSAnnotation> getMSMSWithScores(ScoreType scoreType, List<MSMSAnnotation> libraryMsms, MSMSSearchRequestDTO queryMsms, String queryTolMode, Double tolValue) throws IOException {
        SpectrumScorer comparator = new SpectrumScorer(MzToleranceMode.valueOf(queryTolMode), tolValue);
        var preparedQuery = comparator.prepare(queryMsms.getFragmentsMZsIntensities().getPeaks());
        Set<MSMSAnnotation> matched = new HashSet<>();
        for (MSMSAnnotation lib : libraryMsms) {
            Double libPrecursor = lib.getSpectrum() != null ? lib.getSpectrum().getPrecursorMz() : null;
            Double queryPrecursor = queryMsms.getPrecursorIonMZ();
            double score = comparator.computePrepared(scoreType,
                    comparator.prepare(lib.getSpectrum().getPeaks()),
                    preparedQuery,
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


    private Spectrum copySpectrum(Spectrum source) {
        if (source == null) {
            return null;
        }
        List<MSPeak> copiedPeaks = new ArrayList<>();
        if (source.getPeaks() != null) {
            for (MSPeak peak : source.getPeaks()) {
                copiedPeaks.add(new MSPeak(peak.getMz(), peak.getIntensity()));
            }
        }
        return new Spectrum(source.getPrecursorMz(), copiedPeaks);
    }

}
