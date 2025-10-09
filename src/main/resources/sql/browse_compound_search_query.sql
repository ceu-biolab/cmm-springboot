SELECT
  c.compound_id,
  COALESCE(NULLIF(c.cas_id, ''), cas_lookup.cas_id) AS cas_id,
  c.compound_name,
  c.formula,
  c.mass,
  c.charge_type,
  c.charge_number,
  c.compound_type,
  c.logp,
  c.rt_pred,
  c.inchi,
  c.inchi_key,
  c.smiles,
  c.lipid_type,
  c.num_chains,
  c.number_carbons,
  c.double_bonds,
  lmc.category,
  lmc.main_class,
  lmc.sub_class,
  lmc.class_level4,
  c.biological_activity,
  c.mesh_nomenclature,
  c.iupac_classification,
  c.kegg_id,
  c.lm_id,
  c.hmdb_id,
  c.agilent_id,
  c.pc_id,
  c.chebi_id,
  c.in_house_id,
  c.aspergillus_id,
  c.knapsack_id,
  c.npatlas_id,
  c.fahfa_id,
  c.oh_position,
  c.aspergillus_web_name,
  NULL::text AS mol2
FROM compounds_view c
LEFT JOIN LATERAL (
    SELECT cas_id
    FROM compounds_cas
    WHERE inchi_key = c.inchi_key AND cas_id IS NOT NULL AND cas_id <> ''
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
) lmc ON lmc.compound_id = c.compound_id
WHERE 1=1
  AND (:compoundNameFilter)
  AND c.formula LIKE '(:formula)'
  (:databaseFilterCondition);
