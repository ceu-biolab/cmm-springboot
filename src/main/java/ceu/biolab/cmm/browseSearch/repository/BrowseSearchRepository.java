package ceu.biolab.cmm.browseSearch.repository;


import ceu.biolab.cmm.browseSearch.dto.BrowseQueryResponse;
import ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest;
import ceu.biolab.cmm.rtSearch.dto.CompoundDTO;
import ceu.biolab.cmm.rtSearch.model.compound.CompoundMapper;
import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
            String sql = loadSqlQuery("classpath:sql/browse_compound_search_query.sql");

            // Construcción de parámetros
            List<Compound>compounds = buildParams(queryData,sql);

            // Ejecución de la consulta


            System.out.println(1);
            // Retornar la respuesta
            return new BrowseQueryResponse(compounds);
        }
        private String loadSqlQuery(String resourcePath) throws IOException {
            Resource resource = resourceLoader.getResource(resourcePath);
            return new String(Files.readAllBytes(Paths.get(resource.getURI())));
        }
    private List<Compound> buildParams(BrowseSearchRequest queryData, String sql) {
        MapSqlParameterSource params = new MapSqlParameterSource();
       List<String> databaseConditions = new ArrayList<>();

        if (queryData.getDatabases().contains(Database.HMDB)) {
            databaseConditions.add("h.hmdb_id IS NOT NULL");
        }
        if (queryData.getDatabases().contains(Database.LIPIDMAPS)) {
            databaseConditions.add("l.lm_id IS NOT NULL");
        }
        if (queryData.getDatabases().contains(Database.KEGG)) {
            databaseConditions.add("k.kegg_id IS NOT NULL");
        }
        if (queryData.getDatabases().contains(Database.INHOUSE)) {
            databaseConditions.add("i.in_house_id IS NOT NULL");
        }
        if (queryData.getDatabases().contains(Database.ASPERGILLUS)) {
            databaseConditions.add("a.aspergillus_id IS NOT NULL");
        }
        if (queryData.getDatabases().contains(Database.FAHFA)) {
            databaseConditions.add("f.fahfa_id IS NOT NULL");
        }
        if (queryData.getDatabases().contains(Database.CHEBI)) {
            databaseConditions.add("ch.chebi_id IS NOT NULL");
        }
        if (queryData.getDatabases().contains(Database.PUBCHEM)) {
            databaseConditions.add("p.pc_id IS NOT NULL");
        }
        if (queryData.getDatabases().contains(Database.NPATLAS)) {
            databaseConditions.add("n.npatlas_id IS NOT NULL");
        }
        String filterCondition = databaseConditions.isEmpty() ? "TRUE" : String.join(" OR ", databaseConditions);

        int metaboliteType = 0;
        if(queryData.getMetaboliteType().equals(MetaboliteType.ONLYLIPIDS)){ metaboliteType=1;};
        filterCondition += " AND c.compound_type = " + metaboliteType;
        sql = sql.replace("(:databaseFilterCondition)", filterCondition);

        params.addValue("name", queryData.getCompoundName());
        params.addValue("formula", queryData.getCompoundFormula());

System.out.println(2);
        Set<Compound> compoundsSet = new HashSet<>();

       jdbcTemplate.query(sql, params, rs -> {
            while (rs.next()) {
                System.out.println(3);
                CompoundDTO dto = CompoundMapper.fromResultSet(rs);
                compoundsSet.add(CompoundMapper.toCompound(dto));
            }
            return compoundsSet;
        });
        System.out.println(3);
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info("QUERY: {} ", sql);
        return new ArrayList<>(compoundsSet);



    }
    }

