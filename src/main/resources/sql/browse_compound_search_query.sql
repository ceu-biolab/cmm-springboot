SELECT * FROM compounds_view AS c
WHERE 1=1
  AND (:compoundNameFilter)
  AND c.formula LIKE '(:formula)'
  (:databaseFilterCondition);
