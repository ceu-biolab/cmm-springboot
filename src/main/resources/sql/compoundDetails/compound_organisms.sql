SELECT DISTINCT o.organism_id::text AS id, o.organism_name AS name, NULL::text AS uri
FROM compound_organism c JOIN organism o ON o.organism_id = c.organism_id
WHERE c.compound_id = ? ORDER BY id;
