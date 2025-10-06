package ceu.biolab.cmm.CEMSMarkers.repository;

import ceu.biolab.cmm.CEMSMarkers.domain.MarkerMobility;
import ceu.biolab.cmm.CEMSSearch.domain.CePolarity;
import java.util.Locale;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CemsMarkersRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public CemsMarkersRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<MarkerMobility> findMarkerMobility(String markerName,
                                                       String bufferCode,
                                                       double temperature,
                                                       CePolarity polarity) {
        if (markerName == null || markerName.isBlank()) {
            throw new IllegalArgumentException("marker cannot be null or empty");
        }
        if (bufferCode == null || bufferCode.isBlank()) {
            throw new IllegalArgumentException("buffer cannot be null or empty");
        }

        String sql = """
                SELECT eff_mobility, buffer_id, polarity
                FROM ce_eff_mob_view
                WHERE lower(compound_name) = lower(:marker)
                  AND buffer_code = :buffer
                  AND temperature = :temperature
                  AND polarity = :polarity
                ORDER BY last_updated DESC NULLS LAST, eff_mob_id DESC
                LIMIT 1
                """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("marker", markerName.trim());
        params.addValue("buffer", bufferCode.trim().toUpperCase(Locale.ROOT));
        params.addValue("temperature", Math.round(temperature));
        params.addValue("polarity", polarity.getDatabaseValue());

        return jdbcTemplate.query(sql, params, rs -> {
            if (rs.next()) {
                double effMobility = rs.getDouble("eff_mobility");
                int bufferId = rs.getInt("buffer_id");
                int polarityDb = rs.getInt("polarity");
                CePolarity cePolarity = CePolarity.fromDatabaseValue(polarityDb);
                return Optional.of(new MarkerMobility(effMobility, bufferId, cePolarity));
            }
            return Optional.empty();
        });
    }
}
