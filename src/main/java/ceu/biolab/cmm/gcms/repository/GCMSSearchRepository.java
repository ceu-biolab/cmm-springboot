package ceu.biolab.cmm.gcms.repository;

import ceu.biolab.cmm.ccsSearch.dto.CcsFeatureQueryDTO;
import ceu.biolab.cmm.ccsSearch.dto.CcsQueryResponseDTO;
import ceu.biolab.cmm.gcms.dto.GCMSFeatureQueryDTO;
import ceu.biolab.cmm.gcms.dto.GCMSQueryResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Repository
public class GCMSSearchRepository {
    private NamedParameterJdbcTemplate jdbcTemplate;
    private ResourceLoader resourceLoader;

    @Autowired
    public GCMSSearchRepository(NamedParameterJdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
    }

    public List<GCMSQueryResponseDTO> findMatchingCompounds(GCMSFeatureQueryDTO queryData) throws IOException {
        //Resource resource = resourceLoader.getResource("classpath:sql/ccs_compound_search_query.sql");
        Resource resource = resourceLoader.getResource("");

        String sql = new String(Files.readAllBytes(Paths.get(resource.getURI())));

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("RILower", queryData.getMinRI());
        params.addValue("RIUpper", queryData.getMaxRI());
        params.addValue("DerivatizationType", queryData.getMaxRI());
        params.addValue("ColumnType", queryData.getMaxRI());



        return jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(GCMSQueryResponseDTO.class));
    }
}
