SELECT 
  c.compound_id,
  c.compound_name,
  c.mass AS monoisotopic_mass,
  cc.ccs_value AS db_ccs,
  c.formula,
  c.formula_type,
  c.compound_type,
  c.logp AS log_p,
  cp.pathway_id,
  p.pathway_name,
  p.pathway_map
FROM compounds as c
INNER JOIN compound_ccs as cc
  ON c.compound_id = cc.compound_id
INNER JOIN adduct as a
  ON a.adduct_id = cc.adduct_id
  AND a.adduct_type = :adductType
INNER JOIN buffer_gas as bg
  ON bg.buffer_gas_id = cc.buffer_gas_id
  AND bg.buffer_gas_name = :bufferGasName
LEFT JOIN compounds_pathways as cp 
  ON c.compound_id = cp.compound_id 
LEFT JOIN pathways as p
  ON p.pathway_id = cp.pathway_id
WHERE
  c.mass BETWEEN :massLower AND :massUpper
  AND cc.ccs_value BETWEEN :ccsLower AND :ccsUpper
;
