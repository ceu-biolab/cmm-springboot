package ceu.biolab.cmm.legacyApi.dto;

import java.util.List;

public final class LegacyDetailsRows {
    private LegacyDetailsRows() {}
    public record Link(String id, String name) {}
    public record NumberedLink(String id, String name, Integer number) {}
    public record ClassyfireNode(String node_name, String node_id, String kingdom, String super_class,
            String main_class, String sub_class, String direct_parent, String level_7, String level_8,
            String level_9, String level_10, String level_11, String created, String last_updated) {}
    public record OntologyTerm(Long ontology_term_id, String term, String definition, String external_id,
            String external_source, String ontology_comment, String curator, Long parent_id,
            Integer ontology_level, String created, String last_updated) {}
    public record Reference(Long reference_id, String reference_text, String doi, String link,
            List<Link> compounds, List<Link> organisms, String created, String last_updated) {}
    public record Organism(Long organism_id, String organism_name, Long parent_id, Integer organism_level,
            List<NumberedLink> organism_hierarchy, List<Link> compounds, List<Link> references,
            String created, String last_updated) {}
    public record CompoundInfo(int identifier, String cas_id, String name, String formula, double mass,
            String charge_type, int charge_number, Double logP, String kegg_id, String lm_id, String hmdb_id,
            String metlin_id, Integer pc_id, String aspergillus_web_name, Integer chebi_id, Integer aspergillus_id,
            String knapsack_id, Integer npatlas_id, Integer oh_position, String biological_activity,
            String mesh_nomenclature, String iupac_classification, String inchi, String inchi_key, String smiles,
            Integer num_chains, Integer number_carbons, Integer double_bonds, String category, String main_class,
            String sub_class, String class_level4, List<Link> reactions, List<Link> classyfire_classification,
            List<Link> ontology_terms, List<Link> references, List<Link> organisms, String created, String last_updated) {}
}
