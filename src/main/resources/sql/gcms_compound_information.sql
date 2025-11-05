

-- GETS ALL INFORMATION EXCEPT THE SPECTRUM

-- INPUT VARIABLES: gc_column_name, derivatization_type and RI (range)
-- OUTPUT: compound information, identifiers, spectrum id, RI & RT 
			-- It is not necessary the column name & the derivatization method name since I use them to find my data, so I could access them directly

SELECT cv.compound_id,
       cv.compound_name,
       cv.mass,
       cv.formula,
       cv.logp AS logP,
       cv.cas_id,
       cv.charge_type,
       cv.charge_number,
       cv.compound_type,
       cv.inchi,
       cv.inchi_key,
       cv.smiles,
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
       cv.oh_position AS oh_position_id,
       cv.aspergillus_web_name,
       rirt.RT,
       rirt.RI, -- they will be dbRT & dbRI as arguments of the java class
       s.gcms_spectrum_id
FROM gc_ri_rt as rirt
INNER JOIN derivatization_methods as dm
	ON rirt.derivatization_method_id = dm.derivatization_method_id
INNER JOIN gc_column as col
	ON rirt.gc_column_id = col.gc_column_id
INNER JOIN compounds_view as cv
	ON rirt.compound_id = cv.compound_id
INNER JOIN gcms_spectrum as s
	ON rirt.compound_id = s.compound_id
WHERE rirt.RI BETWEEN :RILower AND :RIUpper
	AND col.gc_column_name = :ColumnType
    AND dm.derivatization_type = :DerivatizationType;
