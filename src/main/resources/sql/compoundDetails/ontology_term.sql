SELECT ontology_term_id, term, definition, external_id, external_source, ontology_comment,
       curator, parent_id, ontology_level, created, last_updated
FROM ontology_terms WHERE ontology_term_id = ?;
