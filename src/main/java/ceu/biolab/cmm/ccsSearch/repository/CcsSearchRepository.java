package ceu.biolab.cmm.ccsSearch.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import ceu.biolab.cmm.ccsSearch.dto.CcsQueryResponseDTO;
import ceu.biolab.cmm.ccsSearch.dto.CcsFeatureQueryDTO;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Repository
public class CcsSearchRepository {
    private NamedParameterJdbcTemplate jdbcTemplate;
    private ResourceLoader resourceLoader;

    @Autowired
    public CcsSearchRepository(NamedParameterJdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
    }

    public List<CcsQueryResponseDTO> findMatchingCompounds(CcsFeatureQueryDTO queryData) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:sql/ccs_compound_search_query.sql");

        String sql;
        try (InputStream inputStream = resource.getInputStream()) {
            sql = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("adductType", queryData.getAdduct());
        params.addValue("bufferGasName", queryData.getBufferGas());
        params.addValue("massLower", queryData.getMassLower());
        params.addValue("massUpper", queryData.getMassUpper());
        params.addValue("ccsLower", queryData.getCcsLower());
        params.addValue("ccsUpper", queryData.getCcsUpper());

        return jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(CcsQueryResponseDTO.class));
    }
}
