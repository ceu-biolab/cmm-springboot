package ceu.biolab.cmm.shared.repository;

import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.compound.Pathway;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.*;

/** Loads pathways in bulk. The supplied cache belongs to a single search request. */
public final class CompoundPathways {
    private CompoundPathways() {}

    public static void hydrate(JdbcTemplate jdbc, Collection<Compound> compounds,
                               Map<Integer, Set<Pathway>> cache) {
        Set<Integer> missing = new LinkedHashSet<>();
        for (Compound compound : compounds) {
            if (!cache.containsKey(compound.getCompoundId())) missing.add(compound.getCompoundId());
        }
        if (!missing.isEmpty()) {
            Map<Integer, Set<Pathway>> loaded = new HashMap<>();
            for (Integer id : missing) loaded.put(id, new HashSet<>());
            var namedJdbc = new NamedParameterJdbcTemplate(jdbc);
            for (var batch : JdbcBatches.partition(missing)) namedJdbc.query("""
                    SELECT cp.compound_id, p.pathway_id, p.pathway_map, p.pathway_name
                    FROM compounds_pathways cp
                    JOIN pathways p ON p.pathway_id = cp.pathway_id
                    WHERE cp.compound_id IN (:ids)
                    """, Map.of("ids", batch), (org.springframework.jdbc.core.RowCallbackHandler) rs -> {
                Pathway pathway = new Pathway();
                pathway.setPathwayId(rs.getInt("pathway_id"));
                pathway.setPathwayMap(rs.getString("pathway_map"));
                pathway.setPathwayName(rs.getString("pathway_name"));
                loaded.get(rs.getInt("compound_id")).add(pathway);
            });
            cache.putAll(loaded);
        }
        for (Compound compound : compounds) {
            Set<Pathway> pathways = new HashSet<>();
            for (Pathway source : cache.get(compound.getCompoundId())) {
                Pathway copy = new Pathway();
                copy.setPathwayId(source.getPathwayId());
                copy.setPathwayMap(source.getPathwayMap());
                copy.setPathwayName(source.getPathwayName());
                pathways.add(copy);
            }
            compound.setPathways(pathways);
        }
    }
}
