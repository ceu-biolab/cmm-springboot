SELECT *
FROM compounds_cmm_complete.compounds AS c
WHERE
    (c.compound_name IS NULL OR c.compound_name ILIKE CONCAT('%', :name, '%'))
  AND (c.compound_formula IS NULL OR c.compound_formula ILIKE CONCAT('%', :formula, '%'))
  AND (c.database IS NULL OR c.database IN (:databases))
  AND (c.metabolite_type IS NULL OR c.metabolite_type = :metaboliteType)