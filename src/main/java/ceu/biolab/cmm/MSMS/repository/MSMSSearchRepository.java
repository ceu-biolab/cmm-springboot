package ceu.biolab.cmm.MSMS.repository;

import ceu.biolab.cmm.MSMS.domain.*;

import ceu.biolab.cmm.MSMS.dto.MSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMS.dto.MSMSSearchResponseDTO;
import ceu.biolab.cmm.MSMS.service.SpectrumScorer;
import ceu.biolab.cmm.msSearch.domain.compound.CompoundMapper;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.adduct.AdductList;
import ceu.biolab.cmm.shared.domain.compound.Compound;
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
        queryMsms.setPrecursorMz(queryData.getPrecursorIonMZ());
        queryMsms.setPeaks(queryData.getSpectrum());

        // Prepare response containers
        Set<Compound> compoundsSet = new HashSet<>();
        Set<MSMSAnotation> matchedSpectra = new HashSet<>();
        MSMSSearchResponseDTO responseDTO = new MSMSSearchResponseDTO(new ArrayList<>(), new ArrayList<>());

        // Determine adduct map based on ionization mode
        Map<String, String> adductMap;
        switch (queryData.getIonizationMode()) {
            case POSITIVE:
                adductMap = AdductList.MAPMZPOSITIVEADDUCTS;
                break;
            case NEGATIVE:
                adductMap = AdductList.MAPMZNEGATIVEADDUCTS;
                break;
            case NEUTRAL:
                adductMap = AdductList.MAPNEUTRALADDUCTS;
                break;
            default:
                throw new IllegalArgumentException("Unsupported ionization mode");
        }

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

            if (queryData.getToleranceModePrecursorIon() == ToleranceMode.PPM) {
                // Tolerancia en PPM: se calcula en función del m/z
                delta = neutralMass * (queryData.getTolerancePrecursorIon() / 1_000_000.0);
            } else {
                // Tolerancia en Da: se calcula de forma directa
                delta = queryData.getTolerancePrecursorIon();
            }
            double lowerBound = neutralMass - delta / 2;
            double upperBound = neutralMass + delta / 2;

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

            //TOdo REVIEW +

            Set<MSMSAnotation> libSpectra= new HashSet<>();
            for (Compound compound : compoundsSet) {
                System.out.println(compound.getCompoundName()+" mass: "+compound.getMass());
                libSpectra.addAll(getSpectraForCompounds(compound, queryData.getIonizationMode(), queryData.getCIDEnergy(), neutralMass));
                matchedSpectra.addAll(getMSMSWithScores(new ArrayList<>(libSpectra), queryMsms,queryData.getToleranceModePrecursorIon().toString(),queryData.getToleranceFragments()));
            }

        }
        responseDTO.setMsmsList(new ArrayList<>(matchedSpectra));
        return responseDTO;
    }

    public List<MSMSAnotation> getSpectraForCompounds(Compound compound,
                                                      IonizationMode ionizationMode,
                                                      CIDEnergy voltageEnergy,
                                                      Double neutralMass) throws IOException {
        Set<MSMSAnotation> msmsSet = getBestMsmsForCompound(compound, ionizationMode, voltageEnergy,neutralMass);
        List<MSMSAnotation> spectra = new ArrayList<>();
        for (MSMSAnotation msms : msmsSet) {
            // Fetch precursor m/z and peaks
            List<Peak> peaks = getPeaksForMsms(String.valueOf(msms.getMsmsId()));
            msms.setPeaks(peaks);
            spectra.add(msms);
        }
        return spectra;
    }

    public Set<MSMSAnotation> getBestMsmsForCompound(Compound compound,
                                                     IonizationMode ionMode,
                                                     CIDEnergy voltage,
                                                     Double neutralMass) throws IOException {
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
            msms.setMsmsId(rs.getInt("msms_id"));
            msms.setPrecursorMz(neutralMass);
             msmsSet.add(msms);}
            return null;
        });
        return msmsSet;
    }

    public List<Peak> getPeaksForMsms(String msmsId) throws IOException {
        Resource rsrc = resourceLoader.getResource("classpath:sql/MSMS/PeakSearch.sql");
        String sql = new String(Files.readAllBytes(Paths.get(rsrc.getURI())));
        sql = sql.replace("(:msmsId)", msmsId);

        Set<Peak> peaks = new HashSet<>();
        jdbcTemplate.query(sql, (rs, rowNum) -> {
            Peak pk = new Peak();
            pk.setMz(rs.getDouble("mz"));
            pk.setIntensity(rs.getDouble("intensity"));
            peaks.add(pk);
            return null;
        });
        return new ArrayList<>(peaks);
    }

    public List<MSMSAnotation> getMSMSWithScores(List<MSMSAnotation> libraryMsms, MSMSAnotation queryMsms, String queryTolMode, Double tolValue) throws IOException {
        SpectrumScorer comparator = new SpectrumScorer( ToleranceMode.valueOf(queryTolMode),tolValue,2,0.5);
        Set<MSMSAnotation> matched = new TreeSet<>();
        for (MSMSAnotation lib : libraryMsms) {

            double score = comparator.compute(SpectrumScorer.ScoreType.COSINE,lib.getPeaks(), queryMsms.getPrecursorMz(),queryMsms.getPeaks(),tolValue);

            lib.setScore(score);
                matched.add(lib);
        }

        return new ArrayList<>(matched);
    }
}
