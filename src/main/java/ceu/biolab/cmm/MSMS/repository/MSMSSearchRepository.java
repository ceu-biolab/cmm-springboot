package ceu.biolab.cmm.MSMS.repository;

import ceu.biolab.cmm.MSMS.domain.CIDEnergy;
import ceu.biolab.cmm.MSMS.domain.MSMS;
import ceu.biolab.cmm.MSMS.domain.Peak;
import ceu.biolab.cmm.MSMS.domain.ToleranceMode;
import ceu.biolab.cmm.MSMS.dto.MSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMS.dto.MSMSSearchResponseDTO;
import ceu.biolab.cmm.msSearch.dto.CompoundDTO;
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
    private NamedParameterJdbcTemplate jdbcTemplate;
    private ResourceLoader resourceLoader;

    @Autowired
    public MSMSSearchRepository(NamedParameterJdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
    }
    public MSMSSearchResponseDTO findMatchingCompoundsAndSpectra(MSMSSearchRequestDTO queryData) throws IOException {
        Double delta=0.0;
        Double lower_bound=0.0;
        Double upper_bound=0.0;

        String resourcePath="classpath:sql/WindowSearch.sql";
        Resource resource1=resourceLoader.getResource(resourcePath);
        String originalSql = new String(Files.readAllBytes(Paths.get(resource1.getURI())));


        MSMSSearchResponseDTO responseDTO= new MSMSSearchResponseDTO(new ArrayList<>(),new ArrayList<>());

        Set<Compound> compoundsSet =  new HashSet<>();

        Map<String, String> adductList = new HashMap<String, String>();
        if (queryData == null) {
        }
        if (queryData.getIonizationMode() == IonizationMode.POSITIVE) {
            adductList = AdductList.MAPMZPOSITIVEADDUCTS;
        }
        if (queryData.getIonizationMode() == IonizationMode.NEGATIVE) {
            adductList = AdductList.MAPMZNEGATIVEADDUCTS;
        }
        if (queryData.getIonizationMode() == IonizationMode.NEUTRAL) {
            adductList = AdductList.MAPNEUTRALADDUCTS;
        }
        for (String queryAdduct : queryData.getAdducts()) {
            if (adductList.containsKey(queryAdduct)) {
                Double neutral_mass = AdductTransformer.getMonoisotopicMassFromMZ(queryData.getPrecursorIonMZ(),queryAdduct,queryData.getIonizationMode() );
                if (queryData.getToleranceModePrecursorIon()== ToleranceMode.PPM) {
                     delta=neutral_mass*(queryData.getTolerancePrecursorIon()/1_000_000.0);
                }else {
                     delta= queryData.getTolerancePrecursorIon()/1000.0;
                }
                lower_bound=neutral_mass-delta;
                upper_bound=neutral_mass+delta;
                String sqlWindow = originalSql;
                sqlWindow = sqlWindow.replace("(:lowerBound)", lower_bound.toString());
                sqlWindow = sqlWindow.replace("(:upperBound)", upper_bound.toString());


                jdbcTemplate.query(sqlWindow, rs -> {

                    while (rs.next()) {
                        CompoundDTO dto = CompoundMapper.fromResultSet(rs);
                        System.out.printf("Searching for compound that goes trhough filter: %s\n",CompoundMapper.toCompound(dto).getCompoundName());
                        compoundsSet.add(CompoundMapper.toCompound(dto));
                    }
                    return compoundsSet;
                });
            }

        }
        // que tiene que devolver ?? Lista de espectros pero de que tipo una clase con id y tal ??
        Set<MSMS> featureSet = new HashSet<>();

        for (Compound compound : compoundsSet) {
            Optional<String> optMsmsId = getBestMsmsForCompound(
                    compound.getCompoundId(),
                    queryData.getIonizationMode(),
                    queryData.getCIDEnergy()
            );

            if (optMsmsId.isEmpty()) continue;

            String msmsId = optMsmsId.get();
            List<Peak> peaks = getPeaksForMsms(msmsId);

            MSMS feature = new MSMS();
            feature.setCompoundId(compound.getCompoundId());
            feature.setMsmsId(Integer.parseInt(msmsId));
            feature.setIonizationMode(queryData.getIonizationMode());
            feature.setVoltageEnergy(queryData.getCIDEnergy());
            feature.setPeaks(peaks);

            featureSet.add(feature);
        }

        responseDTO.setCompoundsList((List<Compound>) compoundsSet);
        responseDTO.setMsmsList((List<MSMS>) featureSet);
        return responseDTO;
    }
    public Optional<String> getBestMsmsForCompound(int compoundId, IonizationMode ionMode, CIDEnergy voltage) {
        String sql = "SELECT * FROM msms WHERE compound_id = :compoundId AND ionization_mode = :ionMode AND voltage_level = :voltage LIMIT 1";

        Map<String, Object> params = Map.of(
                "compoundId", compoundId,
                "ionMode", ionMode.toString(),
                "voltage", voltage
        );

        List<String> msmsIds = jdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getString("msms_id"));
        return msmsIds.isEmpty() ? Optional.empty() : Optional.of(msmsIds.get(0));
    }
    public List<Peak> getPeaksForMsms(String msmsId) {
        String sql = "SELECT mz, intensity FROM msms_peaks WHERE msms_id = :msmsId";

        Map<String, Object> params = Map.of("msmsId", msmsId);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Peak peak = new Peak(rs.getDouble("mz"), rs.getInt("intensity"));

            return peak;
        });
    }
}
