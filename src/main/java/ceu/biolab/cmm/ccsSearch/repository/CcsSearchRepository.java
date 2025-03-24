package ceu.biolab.cmm.ccsSearch.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import ceu.biolab.cmm.ccsSearch.dto.CcsQueryResponse;
import ceu.biolab.cmm.ccsSearch.dto.CcsFeatureQuery;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    public List<CcsQueryResponse> findMatchingCompounds(CcsFeatureQuery queryData) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:sql/ccs_compound_search_query.sql");

        // TODO check materialized views
        String sql = new String(Files.readAllBytes(Paths.get(resource.getURI())));

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("adductType", queryData.getAdduct());
        params.addValue("bufferGasName", queryData.getBufferGas());
        params.addValue("massLower", queryData.getMassLower());
        params.addValue("massUpper", queryData.getMassUpper());
        params.addValue("ccsLower", queryData.getCcsLower());
        params.addValue("ccsUpper", queryData.getCcsUpper());

        return jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(CcsQueryResponse.class));
    }
}
