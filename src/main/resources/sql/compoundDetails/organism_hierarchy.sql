WITH RECURSIVE ancestry AS (
    SELECT organism_id, organism_name, organism_level, parent_id,
           ARRAY[organism_id] AS visited, 0 AS depth, false AS cycle
    FROM organism WHERE organism_id = ?
    UNION ALL
    SELECT o.organism_id, o.organism_name, o.organism_level, o.parent_id,
           a.visited || o.organism_id, a.depth + 1, o.organism_id = ANY(a.visited)
    FROM organism o JOIN ancestry a ON o.organism_id = a.parent_id
    WHERE NOT a.cycle AND a.depth < 100
)
SELECT organism_id, organism_name, organism_level, parent_id, depth, cycle FROM ancestry ORDER BY depth;
