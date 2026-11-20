SELECT DISTINCT c.node_id AS id, d.node_name AS name, NULL::text AS uri
FROM compound_classyfire_classification c
LEFT JOIN classyfire_classification_dictionary d ON d.node_id = c.node_id
WHERE c.compound_id = ? ORDER BY id;
