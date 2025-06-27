package ceu.biolab.cmm.browseSearch.repository;


import ceu.biolab.cmm.browseSearch.dto.BrowseQueryResponse;
import ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest;
import ceu.biolab.cmm.msSearch.dto.CompoundDTO;
import ceu.biolab.cmm.msSearch.domain.compound.CompoundMapper;
import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

                List<Compound> compounds = buildParams(queryData, sql);
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
                databaseConditions.add("c.hmdb_id IS NOT NULL");
            }
            if (queryData.getDatabases().contains(Database.LIPIDMAPS)) {
                databaseConditions.add("c.lm_id IS NOT NULL");
            }
            if (queryData.getDatabases().contains(Database.KEGG)) {
                databaseConditions.add("c.kegg_id IS NOT NULL");
            }
            if (queryData.getDatabases().contains(Database.INHOUSE)) {
                databaseConditions.add("c.in_house_id IS NOT NULL");
            }
            if (queryData.getDatabases().contains(Database.ASPERGILLUS)) {
                databaseConditions.add("c.aspergillus_id IS NOT NULL");
            }
            if (queryData.getDatabases().contains(Database.FAHFA)) {
                databaseConditions.add("c.fahfa_id IS NOT NULL");
            }
            if (queryData.getDatabases().contains(Database.CHEBI)) {
                databaseConditions.add("c.chebi_id IS NOT NULL");
            }
            if (queryData.getDatabases().contains(Database.PUBCHEM)) {
                databaseConditions.add("c.pc_id IS NOT NULL");
            }
            if (queryData.getDatabases().contains(Database.NPATLAS)) {
                databaseConditions.add("c.npatlas_id IS NOT NULL");
            }
            if (queryData.getDatabases().contains(Database.ALL)) {
                databaseConditions.add("c.npatlas_id IS NOT NULL");
                databaseConditions.add("c.lm_id IS NOT NULL");
                databaseConditions.add("c.kegg_id IS NOT NULL");
                databaseConditions.add("c.in_house_id IS NOT NULL");
                databaseConditions.add("c.fahfa_id IS NOT NULL");
                databaseConditions.add("c.pc_id IS NOT NULL");
                databaseConditions.add("c.aspergillus_id IS NOT NULL");
                databaseConditions.add("c.chebi_id IS NOT NULL");
            }
            String filterCondition =  "";

            if (!databaseConditions.isEmpty()) {
                filterCondition += " AND (" + String.join(" OR ", databaseConditions) + ")";
            }

            int metaboliteType = 0;

            if(queryData.getMetaboliteType().equals(MetaboliteType.ONLYLIPIDS)){ metaboliteType=1;};
            filterCondition= filterCondition +" AND c.compound_type = " + metaboliteType;
            sql = sql.replace("(:databaseFilterCondition)", filterCondition);


            if (queryData.isExact_name()){
                sql=sql.replace("(:exact_name)", "LIKE");
                sql=sql.replace("(:compound_name)",queryData.getCompound_name());
            }else {sql=sql.replace("(:exact_name)", "ILIKE");
                sql=sql.replace("(:compound_name)","%"+queryData.getCompound_name()+"%");}

            if(queryData.getCompound_name().isEmpty()){
                sql=sql.replace("(:condition)", "");
            }else    sql=sql.replace("(:condition)", "c.formula IS NULL OR");


            sql=sql.replace("(:formula)", queryData.getFormula());

            Set<Compound> compoundsSet = new HashSet<>();


            jdbcTemplate.query(sql, params, rs -> {

                while (rs.next()) {
                    CompoundDTO dto = CompoundMapper.fromResultSet(rs);
                    System.out.printf("%s\n",CompoundMapper.toCompound(dto).getCompoundName()+" formula: "+CompoundMapper.toCompound(dto).getFormula());
                    compoundsSet.add(CompoundMapper.toCompound(dto));
                }
                return compoundsSet;
            });

            Logger logger = LoggerFactory.getLogger(getClass());
            logger.info("QUERY: {} ", sql);

            return new ArrayList<>(compoundsSet);
    }

}

