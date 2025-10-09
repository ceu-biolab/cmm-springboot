SELECT
  cv.compound_id,
  COALESCE(NULLIF(cv.cas_id, ''), cas_lookup.cas_id) AS cas_id,
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
LEFT JOIN LATERAL (
    SELECT cas_id
    FROM compounds_cas
    WHERE inchi_key = cv.inchi_key AND cas_id IS NOT NULL AND cas_id <> ''
    ORDER BY length(cas_id), cas_id
    LIMIT 1
) cas_lookup ON TRUE
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
WHERE cv.mass BETWEEN (:lowerBound) AND (:upperBound)
(:compoundTypeFilter)
(:databaseFilterCondition);
