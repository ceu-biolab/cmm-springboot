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
            List<String> databaseConditions = Database.databaseConditions(queryData.getDatabases());
            String filterCondition =  "";

            if (!databaseConditions.isEmpty()) {
                filterCondition += " AND (" + String.join(" OR ", databaseConditions) + ")";
            }

            int compoundType = 0;

            if(queryData.getMetaboliteType().equals(MetaboliteType.ONLYLIPIDS)) {
                compoundType=1;
            }

            filterCondition = filterCondition + " AND c.compound_type = " + compoundType;
            sql = sql.replace("(:databaseFilterCondition)", filterCondition);

            String nameFilterBlock = "";
            String compoundName = queryData.getCompoundName();
            if (compoundName != null && compoundName.trim().length() >= 3) {
                String operator = queryData.isExactName() ? "LIKE" : "ILIKE";
                String value = queryData.isExactName() ? compoundName : "%" + compoundName + "%";
                nameFilterBlock = "(c.compound_name " + operator + " '" + value + "')";
                sql = sql.replace("(:compoundNameFilter)", nameFilterBlock);
            }else{
                sql = sql.replace("AND (:compoundNameFilter)", "");
            }

            String formula = queryData.getFormula();
            if (formula == null || formula.isBlank()) {
                sql = sql.replace("(:formula)", "%");
            } else {
                sql = sql.replace("(:formula)", formula);
            }

            Set<Compound> compoundsSet = new HashSet<>();

            jdbcTemplate.query(sql, params, rs -> {
                while (rs.next()) {
                    CompoundDTO dto = CompoundMapper.fromResultSet(rs);
                    Compound compound = CompoundMapper.toCompound(dto);
                    compoundsSet.add(compound);
                    System.out.printf("%s\n",CompoundMapper.toCompound(dto).getCompoundName()+" formula: "+CompoundMapper.toCompound(dto).getFormula());
                }
                return compoundsSet;
            });

            Logger logger = LoggerFactory.getLogger(getClass());
            logger.info("QUERY: {} ", sql);

            return new ArrayList<>(compoundsSet);
    }

}

