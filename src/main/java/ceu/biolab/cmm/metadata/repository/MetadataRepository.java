package ceu.biolab.cmm.metadata.repository;

import ceu.biolab.cmm.metadata.dto.CeMsBufferOption;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MetadataRepository {

    private final JdbcTemplate jdbcTemplate;

    public MetadataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CcsAdductRow> findCcsAdductsInUse() {
        String sql = """
                SELECT DISTINCT a.ionization_mode, a.adduct_type
                FROM adduct a
                JOIN compound_ccs cc ON cc.adduct_id = a.adduct_id
                ORDER BY a.ionization_mode, a.adduct_type
                """;

        return jdbcTemplate.query(sql, (rs, _) -> new CcsAdductRow(
                rs.getLong("ionization_mode"),
                rs.getString("adduct_type")));
    }

    public List<CeMsBufferOption> findCeMsBuffers() {
        String sql = """
                SELECT buffer_id, buffer_code, buffer_description
                FROM ce_buffer_type
                ORDER BY buffer_code
                """;

        return jdbcTemplate.query(sql, (rs, _) -> new CeMsBufferOption(
                rs.getInt("buffer_id"),
                rs.getString("buffer_code"),
                rs.getString("buffer_description")));
    }

    public record CcsAdductRow(long ionizationModeId, String adductType) {
    }
}
