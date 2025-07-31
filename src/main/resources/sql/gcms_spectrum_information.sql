
-- GETS THE SPECTRUM'S INFORMATION

-- INPUT VARIABLES: spectrum Id
-- OUTPUT: spectrum information -> mz & intensity

SELECT p.mz, p.intensity
FROM gcms_spectrum as s -- gcms_spectrum 
INNER JOIN gcms_peaks as p -- peaks con spectrum
	ON s.gcms_spectrum_id = p.gcms_spectrum_id
WHERE s.gcms_spectrum_id = :SpectrumId;
