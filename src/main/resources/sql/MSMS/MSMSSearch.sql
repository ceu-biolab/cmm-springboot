SELECT msms_id, voltage AS ionization_voltage
FROM msms
WHERE compound_id = (:compound_id)
  AND ionization_mode = (:ionization_mode)
  (:voltage_filter_clause)
