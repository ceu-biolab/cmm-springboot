SELECT DISTINCT r.reference_id::text AS id, r.reference_text AS name, r.link AS uri
FROM organism_reference o JOIN "reference" r ON r.reference_id = o.reference_id
WHERE o.organism_id = ? ORDER BY id;
