SELECT DISTINCT c.compound_id::text AS id, c.compound_name AS name, NULL::text AS uri
FROM compound_reference r JOIN compounds c ON c.compound_id = r.compound_id
WHERE r.reference_id = ? ORDER BY id;
