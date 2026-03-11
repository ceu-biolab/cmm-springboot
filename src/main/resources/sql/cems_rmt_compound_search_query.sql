SELECT
  cv.compound_id AS compound_id,
  c.cas_id AS cas_id,
  cv.compound_name AS compound_name,
  cv.formula AS formula,
  cv.mass AS mass,
  cv.charge_type AS charge_type,
  cv.charge_number AS charge_number,
  cv.compound_type AS compound_type,
  cv.logp AS logp,
  cv.rt_pred AS rt_pred,
  cv.inchi AS inchi,
  cv.inchi_key AS inchi_key,
  cv.smiles AS smiles,
  cv.lipid_type AS lipid_type,
  cv.num_chains AS num_chains,
  cv.number_carbons AS number_carbons,
  cv.double_bonds AS double_bonds,
  cv.biological_activity AS biological_activity,
  cv.mesh_nomenclature AS mesh_nomenclature,
  cv.iupac_classification AS iupac_classification,
  cv.kegg_id AS kegg_id,
  cv.lm_id AS lm_id,
  cv.hmdb_id AS hmdb_id,
  cv.agilent_id AS agilent_id,
  cv.pc_id AS pc_id,
  cv.chebi_id AS chebi_id,
  cv.in_house_id AS in_house_id,
  cv.aspergillus_id AS aspergillus_id,
  cv.knapsack_id AS knapsack_id,
  cv.npatlas_id AS npatlas_id,
  cv.fahfa_id AS fahfa_id,
  cv.oh_position AS oh_position,
  cv.aspergillus_web_name AS aspergillus_web_name,
  cv.formula_type_int AS formula_type_int,
  meta.experimental_mz AS experimental_mz,
  meta.exp_eff_mob AS experimental_eff_mob,
  NULL::double precision AS mobility,
  meta.ce_exp_prop_metadata_id AS ce_exp_prop_metadata_id,
  meta.ce_exp_prop_id AS ce_exp_prop_id,
  bt.buffer_code AS buffer_code,
  props.polarity AS polarity_id,
  cep.ionization_mode AS ionization_mode_id,
  meta.relative_mt AS relative_mt,
  meta.absolute_mt AS absolute_mt,
  meta.rmt_ref_compound_id AS rmt_reference_compound_id
FROM ce_experimental_properties_metadata meta
JOIN ce_experimental_properties cep
  ON meta.ce_exp_prop_id = cep.ce_exp_prop_id
JOIN eff_mob_experimental_properties props
  ON cep.eff_mob_exp_prop_id = props.eff_mob_exp_prop_id
JOIN ce_buffer_type bt
  ON props.buffer = bt.buffer_id
JOIN compounds_view cv
  ON meta.compound_id = cv.compound_id
JOIN compounds c
  ON c.compound_id = cv.compound_id
WHERE
  bt.buffer_code = :bufferCode
  AND props.polarity = :polarityId
  AND cep.ionization_mode = :ionizationModeId
  AND props.temperature = :temperature
  AND meta.rmt_ref_compound_id = :referenceCompoundId
  AND cv.mass BETWEEN :massLower AND :massUpper
  AND meta.relative_mt BETWEEN :rmtLower AND :rmtUpper;
