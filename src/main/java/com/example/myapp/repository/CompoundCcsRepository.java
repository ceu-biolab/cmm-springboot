package com.example.myapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.example.myapp.model.CompoundCcsDTO;
import com.example.myapp.api.CcsSearchRequest.CcsRange;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CompoundCcsRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<CompoundCcsDTO> findCompoundsByMultipleCcsRanges(List<CcsRange> ranges) {
        if (ranges == null || ranges.isEmpty()) {
            return new ArrayList<>();
        }

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT c.compound_name, cc.ccs_value ")
                 .append("FROM compounds_cmm.compounds AS c ")
                 .append("JOIN compounds_cmm.compound_ccs AS cc ON c.compound_id = cc.compound_id ")
                 .append("WHERE ");

        List<Object> params = new ArrayList<>();
        
        for (int i = 0; i < ranges.size(); i++) {
            CcsRange range = ranges.get(i);
            double lowerBound = range.getValue() - range.getTolerance();
            double upperBound = range.getValue() + range.getTolerance();
            
            if (i > 0) {
                sqlBuilder.append(" OR ");
            }
            sqlBuilder.append("(cc.ccs_value BETWEEN ? AND ?)");
            params.add(lowerBound);
            params.add(upperBound);
        }

        return jdbcTemplate.query(
                sqlBuilder.toString(),
                (rs, _) -> {
                    CompoundCcsDTO dto = new CompoundCcsDTO();
                    dto.setCompoundName(rs.getString("compound_name"));
                    dto.setCompoundCcs(rs.getDouble("ccs_value"));
                    return dto;
                },
                params.toArray()
        );
    }
}
