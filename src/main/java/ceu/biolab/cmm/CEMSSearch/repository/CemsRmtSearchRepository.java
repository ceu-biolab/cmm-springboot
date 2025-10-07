package ceu.biolab.cmm.CEMSSearch.repository;

import ceu.biolab.cmm.CEMSSearch.dto.CemsQueryResponseDTO;
import ceu.biolab.cmm.CEMSSearch.dto.CemsRmtFeatureQueryDTO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.OptionalLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CemsRmtSearchRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ResourceLoader resourceLoader;

    @Autowired
    public CemsRmtSearchRepository(NamedParameterJdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
    }

    public OptionalLong findReferenceCompoundId(String referenceName) {
        String sql = "SELECT compound_id FROM compounds WHERE lower(compound_name) = lower(:name) LIMIT 1";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", referenceName.trim());

        return jdbcTemplate.query(sql, params, rs -> {
            if (rs.next()) {
                return OptionalLong.of(rs.getLong("compound_id"));
            }
            return OptionalLong.empty();
        });
    }

    public List<CemsQueryResponseDTO> findMatchingCompounds(CemsRmtFeatureQueryDTO queryData) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:sql/cems_rmt_compound_search_query.sql");
        String sql = Files.readString(Paths.get(resource.getURI()));

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("massLower", queryData.getMassLower());
        params.addValue("massUpper", queryData.getMassUpper());
        params.addValue("rmtLower", queryData.getRmtLower());
        params.addValue("rmtUpper", queryData.getRmtUpper());
        params.addValue("bufferCode", queryData.getBufferCode());
        params.addValue("polarityId", queryData.getPolarityId());
        params.addValue("ionizationModeId", queryData.getIonizationModeId());
        params.addValue("temperature", queryData.getTemperature());
        params.addValue("referenceCompoundId", queryData.getReferenceCompoundId());

        return jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(CemsQueryResponseDTO.class));
    }
}
