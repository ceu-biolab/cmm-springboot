SELECT
  cv.compound_id,
  c.cas_id AS cas_id,
  cv.compound_name,
  cv.formula,
  cv.mass,
  cv.charge_type,
  cv.charge_number,
  cv.compound_type,
  cv.logp,
  cv.rt_pred,
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
  cv.biological_activity,
  cv.mesh_nomenclature,
  cv.iupac_classification,
  cv.kegg_id,
  cv.lm_id,
  cv.hmdb_id,
  cv.agilent_id,
  cv.pc_id,
  cv.chebi_id,
  cv.in_house_id,
  cv.aspergillus_id,
  cv.knapsack_id,
  cv.npatlas_id,
  cv.fahfa_id,
  cv.oh_position,
  cv.aspergillus_web_name,
  NULL::text AS mol2
FROM compounds_view cv
JOIN compounds c
  ON c.compound_id = cv.compound_id
LEFT JOIN LATERAL (
    SELECT lm.category, lm.main_class, lm.subclass AS sub_class, lm.class_level4
    FROM compounds_lm_classification clc
    JOIN lm_classification lm ON lm.lm_classification_id = clc.lm_classification_id
    WHERE clc.compound_id = cv.compound_id
    ORDER BY clc.lm_classification_id
    LIMIT 1
) lmc ON TRUE
WHERE cv.mass BETWEEN (:lowerBound) AND (:upperBound)
(:compoundTypeFilter)
(:databaseFilterCondition);
