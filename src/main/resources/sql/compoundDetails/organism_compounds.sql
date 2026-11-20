SELECT DISTINCT c.compound_id::text AS id, c.compound_name AS name, NULL::text AS uri
FROM compound_organism co JOIN compounds c ON c.compound_id = co.compound_id
JOIN organism o ON o.organism_id = co.organism_id
WHERE o.organism_id = ? OR o.parent_id = ? ORDER BY id;
