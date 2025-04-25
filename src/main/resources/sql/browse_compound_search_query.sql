SELECT *
FROM compounds AS c
    LEFT JOIN compounds_hmdb h ON c.compound_id = h.compound_id
    LEFT JOIN compounds_lm  l ON c.compound_id = l.compound_id
    LEFT JOIN compounds_kegg k ON c.compound_id = k.compound_id
    LEFT JOIN compounds_in_house i ON c.compound_id = i.compound_id
    LEFT JOIN compounds_aspergillus a ON c.compound_id = a.compound_id
    LEFT JOIN compounds_fahfa f ON c.compound_id = f.compound_id
    LEFT JOIN compounds_chebi ch ON c.compound_id = ch.compound_id
    LEFT JOIN compounds_npatlas n ON c.compound_id = n.compound_id
WHERE
    (c.compound_name IS NULL OR c.compound_name ILIKE CONCAT('%', :name, '%'))
  AND (c.formula IS NULL OR c.formula ILIKE CONCAT('%', :formula, '%'))
  AND  (:databaseFilterCondition)



