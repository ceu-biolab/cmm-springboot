package ceu.biolab.cmm.MSMS.repository;

import ceu.biolab.cmm.MSMS.domain.*;

import ceu.biolab.cmm.MSMS.dto.MSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMS.dto.MSMSSearchResponseDTO;
import ceu.biolab.cmm.MSMS.service.SpectrumScorer;
import ceu.biolab.cmm.msSearch.domain.compound.CompoundMapper;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import ceu.biolab.cmm.shared.domain.compound.CMMCompound;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.msFeature.MSPeak;
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
        MSMSAnotation queryMsms = new MSMSAnotation();
        Spectrum querySpectrum = new Spectrum(queryData.getPrecursorIonMZ(), queryData.getFragmentsMZsIntensities().getPeaks());
        queryMsms.setSpectrum(querySpectrum);

        // Prepare response containers
        Set<CMMCompound> compoundsSet = new HashSet<>();
        Set<MSMSAnotation> matchedSpectra = new HashSet<>();
        MSMSSearchResponseDTO responseDTO = new MSMSSearchResponseDTO(new ArrayList<>());

        // Determine adduct map based on ionization mode
        Map<String, String> adductMap = AdductProcessing.getAdductMapByIonizationMode(queryData.getIonizationMode());

        // Load window search SQL
        Resource windowResource = resourceLoader.getResource("classpath:sql/MSMS/WindowSearch.sql");
        String windowSql = new String(Files.readAllBytes(Paths.get(windowResource.getURI())));

        // Iterate over each adduct
        for (String adduct : queryData.getAdducts()) {
            if (!adductMap.containsKey(adduct)) {
                throw new IllegalArgumentException("Adduct not found: " + adduct);
            }

            // Compute neutral mass and tolerance window
            double neutralMass = AdductTransformer.getMonoisotopicMassFromMZ(
                    queryData.getPrecursorIonMZ(), adduct, queryData.getIonizationMode()
            );
            double delta = 0.0;  // Variable para la diferencia de tolerancia

            if (queryData.getToleranceModePrecursorIon() == MzToleranceMode.PPM) {
                // Tolerancia en PPM: se calcula en función del m/z
                delta = neutralMass * (queryData.getTolerancePrecursorIon() / 1_000_000.0);
            } else {
                // Tolerancia en Da: se calcula de forma directa
                delta = queryData.getTolerancePrecursorIon();
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

            Set<MSMSAnotation> libSpectra= new HashSet<>();
            for (CMMCompound compound : compoundsSet) {
                System.out.println("Searching spectra for compound: " + compound.getCompoundName() + " (ID: " + compound.getCompoundId() + ")");
                libSpectra.addAll(getSpectraForCompounds(compound, queryData.getIonizationMode(), queryData.getCIDEnergy(), neutralMass));
            }
            matchedSpectra.addAll(getMSMSWithScores(queryData.getScoreType(), new ArrayList<>(libSpectra), queryData,queryData.getToleranceModePrecursorIon().toString(),queryData.getToleranceFragments()));
        }

        matchedSpectra=selectBestPerCompound(new ArrayList<>(matchedSpectra));
        responseDTO.setMsmsList(new ArrayList<>(matchedSpectra));

        return responseDTO;
    }

    public List<MSMSAnotation> getSpectraForCompounds(CMMCompound compound, IonizationMode ionizationMode,
                                                      CIDEnergy voltageEnergy, Double neutralMass) throws IOException {
        Set<MSMSAnotation> msmsSet = getMsmsForCompound(compound, ionizationMode, voltageEnergy,neutralMass);
        List<MSMSAnotation> spectra = new ArrayList<>();
        for (MSMSAnotation msms : msmsSet) {
            // Fetch precursor m/z and peaks
            Spectrum spectrum  = getPeaksForMsms(String.valueOf(msms.getMsmsID()));
            msms.setSpectrum(spectrum);
            spectra.add(msms);
        }
        return spectra;
    }

    public Set<MSMSAnotation> getMsmsForCompound(CMMCompound compound, IonizationMode ionMode,
                                                 CIDEnergy voltage, Double neutralMass) throws IOException {
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

        Set<MSMSAnotation> msmsSet = new HashSet<>();
        jdbcTemplate.query(sql, (rs, rowNum) -> {
           if(!rs.wasNull()){
                MSMSAnotation msms= new MSMSAnotation(compound);
                msms.setPrecursorMz(neutralMass);
                msmsSet.add(msms);}
                return null;
        });
        return msmsSet;
    }

    public Spectrum getPeaksForMsms(String msmsId) throws IOException {
        Resource rsrc = resourceLoader.getResource("classpath:sql/MSMS/PeakSearch.sql");
        String sql = new String(Files.readAllBytes(Paths.get(rsrc.getURI())));
        sql = sql.replace("(:msmsId)", msmsId);

        Set<MSPeak> peaks = new HashSet<>();
        jdbcTemplate.query(sql, (rs, rowNum) -> {
            double mz = rs.getDouble("mz");
            double intensity = rs.getDouble("intensity");
            MSPeak pk = new MSPeak(mz, intensity);
            System.out.println("Peak found: " + pk);
            peaks.add(pk);
            return null;
        });
        return new Spectrum(new ArrayList<>(peaks));
    }

    public List<MSMSAnotation> getMSMSWithScores(ScoreType scoreType, List<MSMSAnotation> libraryMsms, MSMSSearchRequestDTO queryMsms, String queryTolMode, Double tolValue) throws IOException {
        SpectrumScorer comparator = new SpectrumScorer(MzToleranceMode.valueOf(queryTolMode),tolValue);
        Set<MSMSAnotation> matched = new TreeSet<>();
        for (MSMSAnotation lib : libraryMsms) {
            double score = comparator.compute(scoreType,lib.getSpectrum(),queryMsms.getFragmentsMZsIntensities());
            System.out.println("Score for compound " + lib.getCompoundId() + ": " + score);
            lib.setMSMSCosineScore(score);
                matched.add(lib);
        }
        return new ArrayList<>(matched);
    }

    public static Set<MSMSAnotation> selectBestPerCompound(List<MSMSAnotation> allSpectra) {
        Map<String, MSMSAnotation> best = new HashMap<>();
        for (MSMSAnotation sp : allSpectra) {
            best.compute(String.valueOf(sp.getCompoundId()), (id, currentBest) -> {
                if (currentBest == null || sp.getMSMSCosineScore() > currentBest.getMSMSCosineScore()) {
                    return sp;
                } else {
                    return currentBest;
                }
            });
        }
        return new HashSet<> (best.values());
    }

}
