package ceu.biolab.cmm.browseSearch.repository;


import ceu.biolab.cmm.browseSearch.dto.BrowseQueryResponse;
import ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest;
import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
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
import java.util.stream.Collectors;

@Repository
    public class BrowseSearchRepository {

        private NamedParameterJdbcTemplate jdbcTemplate;
        private ResourceLoader resourceLoader;

        @Autowired
        public BrowseSearchRepository(NamedParameterJdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) {
            this.jdbcTemplate = jdbcTemplate;
            this.resourceLoader = resourceLoader;
        }

        public BrowseQueryResponse findMatchingCompounds(BrowseSearchRequest queryData) throws IOException {
            Resource resource = resourceLoader.getResource("classpath:sql/browse_compound_search_query.sql");
            String sql = new String(Files.readAllBytes(Paths.get(resource.getURI())));

            MapSqlParameterSource params = new MapSqlParameterSource();

            List<Database> dbEnums = queryData.getDatabases()
                    .stream()
                    .map(database -> Database.valueOf(database.getName().toUpperCase()))
                    .toList();

            List<String> dbNames = dbEnums.stream()
                    .map(Database::name)
                    .collect(Collectors.toList());

            MetaboliteType metEnum = MetaboliteType.valueOf(queryData.getMetaboliteType().getName().toUpperCase());


            params.addValue("name", queryData.getCompoundName());
            params.addValue("formula", queryData.getCompoundFormula());
            params.addValue("databases", dbNames);
            params.addValue("metaboliteType", metEnum);

            return new BrowseQueryResponse( jdbcTemplate.query(sql, params, new BeanPropertyRowMapper(BrowseQueryResponse.class)));
        }
    }

