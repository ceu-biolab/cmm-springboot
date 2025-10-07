package ceu.biolab.cmm.CEMSSearch.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import ceu.biolab.cmm.CEMSSearch.dto.CemsFeatureQueryDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsQueryResponseDTO;

@Repository
public class CemsSearchRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ResourceLoader resourceLoader;

    @Autowired
    public CemsSearchRepository(NamedParameterJdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
    }

    public List<CemsQueryResponseDTO> findMatchingCompounds(CemsFeatureQueryDTO queryData) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:sql/cems_compound_search_query.sql");
        String sql = Files.readString(Paths.get(resource.getURI()));

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("massLower", queryData.getMassLower());
        params.addValue("massUpper", queryData.getMassUpper());
        params.addValue("mobilityLower", queryData.getMobilityLower());
        params.addValue("mobilityUpper", queryData.getMobilityUpper());
        params.addValue("bufferCode", queryData.getBufferCode());
        params.addValue("polarityId", queryData.getPolarityId());
        params.addValue("ionizationModeId", queryData.getIonizationModeId());

        return jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(CemsQueryResponseDTO.class));
    }
}
