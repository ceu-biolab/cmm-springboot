package ceu.biolab.cmm.compoundDetails.repository;

import ceu.biolab.cmm.compoundDetails.dto.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.*;

@Repository
public class CompoundDetailsRepository {
    private final JdbcTemplate jdbc;
    private final Map<String, String> queries;
    private static final RowMapper<EntityLink> LINK = (rs, row) ->
            new EntityLink(rs.getString("id"), rs.getString("name"), rs.getString("uri"));

    public CompoundDetailsRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        Map<String, String> loaded = new HashMap<>();
        for (String name : List.of("classyfire_node", "ontology_term", "reference", "organism", "compound_metadata",
                "compound_reactions", "compound_classyfire", "compound_ontology", "compound_references", "compound_organisms",
                "reference_compounds", "reference_organisms", "organism_compounds", "organism_references", "organism_hierarchy")) {
            try (var stream = new ClassPathResource("sql/compoundDetails/" + name + ".sql").getInputStream()) {
                loaded.put(name, new String(stream.readAllBytes(), StandardCharsets.UTF_8));
            } catch (IOException ex) {
                throw new UncheckedIOException("Unable to load compound detail query " + name, ex);
            }
        }
        this.queries = Map.copyOf(loaded);
    }

    public Optional<ClassyfireNodeResponse> findClassyfire(String nodeId) {
        return first(jdbc.query(queries.get("classyfire_node"), (rs, row) -> {
            String id = rs.getString("node_id");
            List<String> ancestry = Arrays.asList(rs.getString("kingdom"), rs.getString("super_class"),
                    rs.getString("main_class"), rs.getString("sub_class"), rs.getString("level_5"),
                    rs.getString("level_6"), rs.getString("level_7"), rs.getString("level_8"),
                    rs.getString("level_9"), rs.getString("level_10"));
            String parent = null;
            for (String ancestor : ancestry) {
                if (ancestor != null && !ancestor.isBlank() && !ancestor.equals(id)) parent = ancestor;
            }
            return new ClassyfireNodeResponse(id, rs.getString("node_name"), ancestry.get(0), ancestry.get(1),
                    ancestry.get(2), ancestry.get(3), ancestry.get(4), ancestry.get(5), ancestry.get(6), ancestry.get(7),
                    ancestry.get(8), ancestry.get(9), parent, time(rs, "created"), time(rs, "last_updated"));
        }, nodeId));
    }

    public Optional<OntologyTermResponse> findOntology(long id) {
        return first(jdbc.query(queries.get("ontology_term"), (rs, row) -> new OntologyTermResponse(
                number(rs, "ontology_term_id"), rs.getString("term"), rs.getString("definition"),
                rs.getString("external_id"), rs.getString("external_source"), rs.getString("ontology_comment"),
                rs.getString("curator"), number(rs, "parent_id"), integer(rs, "ontology_level"),
                time(rs, "created"), time(rs, "last_updated")), id));
    }

    public Optional<ReferenceResponse> findReference(long id) {
        return first(jdbc.query(queries.get("reference"), (rs, row) -> new ReferenceResponse(
                number(rs, "reference_id"), rs.getString("reference_text"), rs.getString("doi"), rs.getString("link"),
                time(rs, "created"), time(rs, "last_updated"), List.of(), List.of()), id));
    }

    public Optional<OrganismResponse> findOrganism(long id) {
        return first(jdbc.query(queries.get("organism"), (rs, row) -> new OrganismResponse(
                number(rs, "organism_id"), rs.getString("organism_name"), number(rs, "parent_id"), integer(rs, "organism_level"),
                time(rs, "created"), time(rs, "last_updated"), List.of(), List.of(), List.of()), id));
    }

    public Optional<CompoundMetadata> findCompoundMetadata(long id) {
        return first(jdbc.query(queries.get("compound_metadata"), (rs, row) ->
                new CompoundMetadata(time(rs, "created"), time(rs, "last_updated")), id));
    }

    public List<EntityLink> compoundReactions(long id) { return links("compound_reactions", id); }
    public List<EntityLink> compoundClassyfire(long id) { return links("compound_classyfire", id); }
    public List<EntityLink> compoundOntology(long id) { return links("compound_ontology", id); }
    public List<EntityLink> compoundReferences(long id) { return links("compound_references", id); }
    public List<EntityLink> compoundOrganisms(long id) { return links("compound_organisms", id); }
    public List<EntityLink> referenceCompounds(long id) { return links("reference_compounds", id); }
    public List<EntityLink> referenceOrganisms(long id) { return links("reference_organisms", id); }
    public List<EntityLink> organismReferences(long id) { return links("organism_references", id); }
    public List<EntityLink> organismCompounds(long id) { return jdbc.query(queries.get("organism_compounds"), LINK, id, id); }

    public List<HierarchyEntry> organismHierarchy(long id) {
        return jdbc.query(queries.get("organism_hierarchy"), (rs, row) -> {
            if (rs.getBoolean("cycle") || (rs.getInt("depth") == 100 && rs.getObject("parent_id") != null)) {
                throw new IllegalStateException("Invalid organism hierarchy for " + id);
            }
            return new HierarchyEntry(rs.getString("organism_id"), rs.getString("organism_name"), integer(rs, "organism_level"));
        }, id);
    }

    private List<EntityLink> links(String name, long id) { return jdbc.query(queries.get(name), LINK, id); }
    private static <T> Optional<T> first(List<T> rows) { return rows.stream().findFirst(); }
    private static Long number(ResultSet rs, String column) throws SQLException {
        Number value = (Number) rs.getObject(column);
        return value == null ? null : value.longValue();
    }
    private static Integer integer(ResultSet rs, String column) throws SQLException {
        Number value = (Number) rs.getObject(column);
        return value == null ? null : value.intValue();
    }
    private static OffsetDateTime time(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, OffsetDateTime.class);
    }
}
