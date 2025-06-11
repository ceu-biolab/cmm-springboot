SELECT msms_id
FROM msms
WHERE compound_id = (:compound_id)
  AND ionization_mode = (:ionization_mode)
  AND voltage_level = '(:voltage_level)';