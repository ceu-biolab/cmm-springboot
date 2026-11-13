SELECT DISTINCT t.ontology_term_id::text AS id, t.term AS name, NULL::text AS uri
FROM compound_ontology_terms c JOIN ontology_terms t ON t.ontology_term_id = c.ontology_term_id
WHERE c.compound_id = ? ORDER BY id;
