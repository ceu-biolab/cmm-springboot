package ceu.biolab.cmm.browseSearch.repository;

import ceu.biolab.cmm.shared.repository.SqlQueryLoader;
import ceu.biolab.cmm.shared.repository.CompoundPathways;

import ceu.biolab.cmm.browseSearch.dto.BrowseQueryResponse;
import ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest;
import ceu.biolab.cmm.msSearch.dto.CompoundDTO;
import ceu.biolab.cmm.msSearch.domain.compound.CompoundMapper;
import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.compound.CompoundType;
import ceu.biolab.cmm.shared.service.FormulaTypeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class BrowseSearchRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowseSearchRepository.class);

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlQueryLoader sqlQueries;

    @Autowired
    public BrowseSearchRepository(NamedParameterJdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.sqlQueries = new SqlQueryLoader(resourceLoader);
    }

    public BrowseQueryResponse findMatchingCompounds(BrowseSearchRequest queryData) throws IOException {
        String sqlTemplate = sqlQueries.load("classpath:sql/browse_compound_search_query.sql");
        QueryParts queryParts = buildQuery(sqlTemplate, queryData);
        List<Compound> compounds = executeQuery(queryParts);
        Optional<Set<String>> allowedElements = FormulaTypeFilter.allowedElements(queryData.getFormulaType());
        List<Compound> filteredCompounds = compounds.stream()
                .filter(compound -> FormulaTypeFilter.matches(compound, allowedElements))
                .collect(Collectors.toCollection(ArrayList::new));
        CompoundPathways.hydrate(
                jdbcTemplate.getJdbcTemplate(), filteredCompounds, new java.util.HashMap<>());
        return new BrowseQueryResponse(filteredCompounds);
    }


    private QueryParts buildQuery(String sqlTemplate, BrowseSearchRequest request) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = sqlTemplate;

        Set<Database> databases = request.getDatabases() == null ? Set.of() : request.getDatabases();
        List<String> databaseConditions = Database.databaseConditions(databases);
        StringBuilder filterCondition = new StringBuilder();

        if (!databaseConditions.isEmpty()) {
            filterCondition.append(" AND (").append(String.join(" OR ", databaseConditions)).append(")");
        }

        if (request.getMetaboliteType() == MetaboliteType.ONLYLIPIDS) {
            filterCondition.append(" AND c.compound_type = :compoundType");
            params.addValue("compoundType", CompoundType.LIPID.getDbValue());
        }
        sql = sql.replace("(:databaseFilterCondition)", filterCondition.toString());

        String compoundName = request.getCompoundName();
        if (compoundName != null && compoundName.trim().length() >= 3) {
            String operator = request.isExactName() ? "=" : "ILIKE";
            String value = request.isExactName() ? compoundName.trim() : "%" + escapeLike(compoundName.trim()) + "%";
            sql = sql.replace("(:compoundNameFilter)", "(c.compound_name " + operator + " :compoundName)");
            params.addValue("compoundName", value);
        } else {
            sql = sql.replace("AND (:compoundNameFilter)", "");
        }

        String formula = request.getFormula();
        if (formula != null && !formula.isBlank()) {
            sql = sql.replace("(:formulaFilter)", " AND c.formula = :formula");
            params.addValue("formula", formula.trim());
        } else {
            sql = sql.replace("(:formulaFilter)", "");
        }

        return new QueryParts(sql, params);
    }

    private List<Compound> executeQuery(QueryParts queryParts) {
        Set<Compound> compoundsSet = new HashSet<>();

        jdbcTemplate.query(queryParts.sql(), queryParts.params(), rs -> {
                while (rs.next()) {
                    CompoundDTO dto = CompoundMapper.fromResultSet(rs);
                    Compound compound = CompoundMapper.toCompound(dto);
                    compoundsSet.add(compound);
                }
                return compoundsSet;
        });

        LOGGER.debug("browseSearch executed; matched {} compounds", compoundsSet.size());

        return new ArrayList<>(compoundsSet);
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private record QueryParts(String sql, MapSqlParameterSource params) {}

}
