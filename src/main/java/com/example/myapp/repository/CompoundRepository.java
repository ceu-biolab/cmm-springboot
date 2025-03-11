package com.example.myapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CompoundRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Double findMassByCompoundName(String compoundName) {
        String sql = "SELECT mass FROM compounds_cmm.compounds WHERE compound_name = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Double.class, compoundName);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
