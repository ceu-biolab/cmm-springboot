SELECT * FROM compounds_view AS c
WHERE
    (c.compound_name IS NULL OR c.compound_name (:exact_name) '(:compound_name)')
AND (c.formula IS NULL OR c.formula LIKE '(:formula)')
  (:databaseFilterCondition);



