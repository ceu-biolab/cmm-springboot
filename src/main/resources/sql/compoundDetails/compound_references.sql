SELECT DISTINCT r.reference_id::text AS id, r.reference_text AS name, r.link AS uri
FROM compound_reference c JOIN "reference" r ON r.reference_id = c.reference_id
WHERE c.compound_id = ? ORDER BY id;
