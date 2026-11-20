package ceu.biolab.cmm.ccsSearch.repository;

import ceu.biolab.cmm.shared.repository.SqlQueryLoader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import ceu.biolab.cmm.ccsSearch.dto.CcsQueryResponseDTO;
import ceu.biolab.cmm.ccsSearch.dto.CcsFeatureQueryDTO;

import java.io.IOException;
import java.util.List;

@Repository
public class CcsSearchRepository {
    private NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlQueryLoader sqlQueries;

    @Autowired
    public CcsSearchRepository(NamedParameterJdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.sqlQueries = new SqlQueryLoader(resourceLoader);
    }

    public List<CcsQueryResponseDTO> findMatchingCompounds(CcsFeatureQueryDTO queryData) throws IOException {
        String sql = sqlQueries.load("classpath:sql/ccs_compound_search_query.sql");

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
