package ceu.biolab.cmm.metadata.repository;

import ceu.biolab.cmm.metadata.dto.CeMsBufferOption;
import ceu.biolab.cmm.metadata.dto.DatabaseStatsResponse;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MetadataRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public MetadataRepository(JdbcTemplate jdbcTemplate,
                              NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
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

    public List<CeMsCompoundOptionRow> findCeMsMarkerOptions(String bufferCode,
                                                             Long temperature,
                                                             Integer polarityId,
                                                             Integer ionizationModeId) {
        StringBuilder sql = new StringBuilder("""
                SELECT DISTINCT
                    bt.buffer_id,
                    TRIM(bt.buffer_code) AS buffer_code,
                    bt.buffer_description,
                    props.temperature,
                    props.polarity,
                    cep.ionization_mode,
                    em.compound_id,
                    c.compound_name
                FROM eff_mob em
                JOIN eff_mob_experimental_properties props
                  ON em.eff_mob_exp_prop_id = props.eff_mob_exp_prop_id
                JOIN ce_experimental_properties cep
                  ON cep.eff_mob_exp_prop_id = props.eff_mob_exp_prop_id
                JOIN ce_buffer_type bt
                  ON props.buffer = bt.buffer_id
                JOIN compounds c
                  ON c.compound_id = em.compound_id
                WHERE 1 = 1
                """);

        MapSqlParameterSource params = new MapSqlParameterSource();
        addCeMsOptionFilters(sql, params, bufferCode, temperature, polarityId, ionizationModeId);
        sql.append("""
                ORDER BY buffer_code, props.temperature, props.polarity,
                         cep.ionization_mode, c.compound_name, em.compound_id
                """);

        return namedParameterJdbcTemplate.query(sql.toString(), params, (rs, _) -> new CeMsCompoundOptionRow(
                rs.getLong("buffer_id"),
                rs.getString("buffer_code"),
                rs.getString("buffer_description"),
                rs.getLong("temperature"),
                rs.getInt("polarity"),
                rs.getInt("ionization_mode"),
                rs.getLong("compound_id"),
                rs.getString("compound_name")));
    }

    public List<CeMsCompoundOptionRow> findCeMsRmtReferenceOptions(String bufferCode,
                                                                   Long temperature,
                                                                   Integer polarityId,
                                                                   Integer ionizationModeId) {
        StringBuilder sql = new StringBuilder("""
                SELECT DISTINCT
                    bt.buffer_id,
                    TRIM(bt.buffer_code) AS buffer_code,
                    bt.buffer_description,
                    props.temperature,
                    props.polarity,
                    cep.ionization_mode,
                    meta.rmt_ref_compound_id AS compound_id,
                    c.compound_name
                FROM ce_experimental_properties_metadata meta
                JOIN ce_experimental_properties cep
                  ON meta.ce_exp_prop_id = cep.ce_exp_prop_id
                JOIN eff_mob_experimental_properties props
                  ON cep.eff_mob_exp_prop_id = props.eff_mob_exp_prop_id
                JOIN ce_buffer_type bt
                  ON props.buffer = bt.buffer_id
                JOIN compounds c
                  ON c.compound_id = meta.rmt_ref_compound_id
                WHERE meta.rmt_ref_compound_id IS NOT NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource();
        addCeMsOptionFilters(sql, params, bufferCode, temperature, polarityId, ionizationModeId);
        sql.append("""
                ORDER BY buffer_code, props.temperature, props.polarity,
                         cep.ionization_mode, c.compound_name, meta.rmt_ref_compound_id
                """);

        return namedParameterJdbcTemplate.query(sql.toString(), params, (rs, _) -> new CeMsCompoundOptionRow(
                rs.getLong("buffer_id"),
                rs.getString("buffer_code"),
                rs.getString("buffer_description"),
                rs.getLong("temperature"),
                rs.getInt("polarity"),
                rs.getInt("ionization_mode"),
                rs.getLong("compound_id"),
                rs.getString("compound_name")));
    }

    public DatabaseStatsResponse fetchDatabaseStats() {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM compounds) AS compounds,
                    (SELECT COUNT(*) FROM gcms_spectrum) AS gcms_spectra,
                    (SELECT COUNT(*) FROM msms) AS msms_spectra,
                    (SELECT COUNT(*) FROM compound_ccs) AS ccs_records,
                    (SELECT COUNT(*) FROM ce_experimental_properties_metadata) AS cems_records
                """;

        return jdbcTemplate.queryForObject(sql, (rs, _) -> new DatabaseStatsResponse(
                rs.getLong("compounds"),
                rs.getLong("gcms_spectra"),
                rs.getLong("msms_spectra"),
                rs.getLong("ccs_records"),
                rs.getLong("cems_records")));
    }

    private void addCeMsOptionFilters(StringBuilder sql,
                                      MapSqlParameterSource params,
                                      String bufferCode,
                                      Long temperature,
                                      Integer polarityId,
                                      Integer ionizationModeId) {
        if (bufferCode != null) {
            sql.append("  AND TRIM(bt.buffer_code) = :bufferCode\n");
            params.addValue("bufferCode", bufferCode);
        }
        if (temperature != null) {
            sql.append("  AND props.temperature = :temperature\n");
            params.addValue("temperature", temperature);
        }
        if (polarityId != null) {
            sql.append("  AND props.polarity = :polarityId\n");
            params.addValue("polarityId", polarityId);
        }
        if (ionizationModeId != null) {
            sql.append("  AND cep.ionization_mode = :ionizationModeId\n");
            params.addValue("ionizationModeId", ionizationModeId);
        }
    }

    public record CcsAdductRow(long ionizationModeId, String adductType) {
    }

    public record CeMsCompoundOptionRow(long bufferId,
                                        String bufferCode,
                                        String bufferDescription,
                                        long temperature,
                                        int polarityId,
                                        int ionizationModeId,
                                        long compoundId,
                                        String compoundName) {
    }
}
