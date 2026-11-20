SELECT DISTINCT reaction_id AS id, reaction_id AS name, NULL::text AS uri
FROM compounds_reactions_kegg WHERE compound_id = ? ORDER BY id;
