

-- GETS ALL INFORMATION EXCEPT THE SPECTRUM

-- INPUT VARIABLES: gc_column_name, derivatization_type and RI (range)
-- OUTPUT: compound information, identifiers, spectrum id, RI & RT 
			-- It is not necessary the column name & the derivatization method name since I use them to find my data, so I could access them directly

SELECT c.compound_id, c.compound_name, c.mass, c.formula, c.formula_type, c.logP,
		i.inchi, i.inchi_key, i.smiles, 
        rirt.RT, rirt.RI, -- they will be dbRT & dbRI as arguments of the java class
        s.gcms_spectrum_id
FROM gc_ri_rt as rirt
INNER JOIN derivatization_methods as dm
	ON rirt.derivatization_method_id = dm.derivatization_method_id
INNER JOIN gc_column as col
	ON rirt.gc_column_id = col.gc_column_id
INNER JOIN compounds as c
	ON rirt.compound_id = c.compound_id
INNER JOIN gcms_spectrum as s
	ON rirt.compound_id = s.compound_id
INNER JOIN compound_identifiers as i 
	ON rirt.compound_id = i.compound_id
WHERE rirt.RI BETWEEN :RILower AND :RIUpper
	AND col.gc_column_name = :ColumnType
    AND dm.derivatization_type = :DerivatizationType;