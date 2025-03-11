package com.example.myapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.example.myapp.model.CompoundCcsDTO;
import java.util.List;

@Repository
public class CompoundCcsRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<CompoundCcsDTO> findCompoundsByCcsRange(double lowerBound, double upperBound) {
        String sql = "SELECT c.compound_name, cc.ccs_value " +
                     "FROM compounds_cmm.compounds AS c " +
                     "JOIN compounds_cmm.compound_ccs AS cc ON c.compound_id = cc.compound_id " +
                     "WHERE cc.ccs_value BETWEEN ? AND ?";
        return jdbcTemplate.query(
                sql,
                (rs, _) -> {
                    CompoundCcsDTO dto = new CompoundCcsDTO();
                    dto.setCompoundName(rs.getString("compound_name"));
                    dto.setCompoundCcs(rs.getDouble("ccs_value"));
                    return dto;
                },
                lowerBound, upperBound
        );
    }
}
