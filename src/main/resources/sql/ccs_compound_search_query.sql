SELECT 
  cv.compound_id,
  cv.cas_id,
  cv.compound_name,
  cv.formula,
  cv.mass AS monoisotopic_mass,
  cv.charge_type,
  cv.charge_number,
  cv.formula_type_int,
  cv.compound_type,
  cv.logp AS log_p,
  cv.rt_pred,
  cv.biological_activity,
  cv.mesh_nomenclature,
  cv.iupac_classification,
  cv.inchi,
  cv.inchi_key,
  cv.smiles,
  cv.lipid_type,
  cv.num_chains,
  cv.number_carbons,
  cv.double_bonds,
  lmc.category,
  lmc.main_class,
  lmc.sub_class,
  lmc.class_level4,
  cc.ccs_value AS db_ccs,
  cp.pathway_id,
  p.pathway_name,
  p.pathway_map
FROM compounds_view AS cv
INNER JOIN compound_ccs AS cc
  ON cv.compound_id = cc.compound_id
INNER JOIN adduct as a
  ON a.adduct_id = cc.adduct_id
  AND a.adduct_type = :adductType
INNER JOIN buffer_gas as bg
  ON bg.buffer_gas_id = cc.buffer_gas_id
  AND bg.buffer_gas_name = :bufferGasName
LEFT JOIN compounds_pathways as cp 
  ON cv.compound_id = cp.compound_id 
LEFT JOIN pathways as p
  ON p.pathway_id = cp.pathway_id
LEFT JOIN (
    SELECT
      clc.compound_id,
      max(lm.category) AS category,
      max(lm.main_class) AS main_class,
      max(lm.subclass) AS sub_class,
      max(lm.class_level4) AS class_level4
    FROM compounds_lm_classification clc
    JOIN lm_classification lm ON lm.lm_classification_id = clc.lm_classification_id
    GROUP BY clc.compound_id
) lmc ON lmc.compound_id = cv.compound_id
WHERE
  cv.mass BETWEEN :massLower AND :massUpper
  AND cc.ccs_value BETWEEN :ccsLower AND :ccsUpper
;
