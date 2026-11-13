SELECT DISTINCT o.organism_id::text AS id, o.organism_name AS name, NULL::text AS uri
FROM organism_reference r JOIN organism o ON o.organism_id = r.organism_id
WHERE r.reference_id = ? ORDER BY id;
