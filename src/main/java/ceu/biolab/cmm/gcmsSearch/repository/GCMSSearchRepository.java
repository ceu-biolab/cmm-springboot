package ceu.biolab.cmm.gcmsSearch.repository;

import ceu.biolab.cmm.shared.repository.SqlQueryLoader;
import ceu.biolab.cmm.shared.repository.JdbcBatches;

import ceu.biolab.cmm.gcmsSearch.domain.ColumnType;
import ceu.biolab.cmm.gcmsSearch.domain.DerivatizationMethod;
import ceu.biolab.cmm.gcmsSearch.domain.GCMSCompound;
import ceu.biolab.cmm.gcmsSearch.dto.GCMSFeatureQueryDTO;
import ceu.biolab.cmm.gcmsSearch.dto.GCMSQueryResponseDTO;
import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.shared.domain.compound.CompoundType;
import ceu.biolab.cmm.shared.domain.msFeature.Peak;
import ceu.biolab.cmm.shared.domain.msFeature.Spectrum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class GCMSSearchRepository {
    private NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlQueryLoader sqlQueries;

    @Autowired
    public GCMSSearchRepository(NamedParameterJdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.sqlQueries = new SqlQueryLoader(resourceLoader);
    }

    /**
     * With the information given by the user (queryData) and using a query,
     * it gets the compound information from the database & creates the GCMSCompound
     *
     * @param queryData  GCMSFeatureQueryDTO that contains the information that is needed for the search
     * @return a list of GCMSCompound with the information from the database
     * @throws IOException
     */
    private List<GCMSCompound> getCompoundInformation(GCMSFeatureQueryDTO queryData) throws IOException {
        String sql1 = sqlQueries.load("classpath:sql/gcms_compound_information.sql");

        MapSqlParameterSource params1 = new MapSqlParameterSource();
        params1.addValue("RILower", queryData.getMinRI());
        params1.addValue("RIUpper", queryData.getMaxRI());
        //SINCE BOTH PARAMETERS ARE ENUMS I WILL USE .name() TO CONVERT THEM TO STRING
        params1.addValue("ColumnType", queryData.getColumnType().name());
        params1.addValue("DerivatizationType", queryData.getDerivatizationMethod().name());

        /*
        SINCE THE INFORMATION OBTAINED FROM THE DATABASE CORRESPONDS TO TWO DIFFERENT CLASSES,
        A RowMapper IS USED, AS IT ALLOWS TO STORE THE DATA IN DIFFERENT CLASSES
        */
        RowMapper<GCMSCompound> gcmsCompoundCustomMapper = (rs, _) -> {
            //CREATION OF THE GCMSCompound
            Number compoundTypeDb = (Number) rs.getObject("compound_type");
            Integer compoundTypeRaw = compoundTypeDb == null ? null : compoundTypeDb.intValue();
            CompoundType compoundType = CompoundType.fromDbValue(compoundTypeRaw);
            if (compoundType == null) {
                compoundType = CompoundType.NON_LIPID;
            }

            FormulaType inferredFormulaType = FormulaType.inferFromFormula(rs.getString("formula")).orElse(null);

            Object pcIdObj = rs.getObject("pc_id");
            Integer pcId = pcIdObj == null ? null : ((Number) pcIdObj).intValue();

            Object chebiIdObj = rs.getObject("chebi_id");
            Integer chebiId = chebiIdObj == null ? null : ((Number) chebiIdObj).intValue();

            Object aspergillusIdObj = rs.getObject("aspergillus_id");
            Integer aspergillusId = aspergillusIdObj == null ? null : ((Number) aspergillusIdObj).intValue();

            Object npatlasIdObj = rs.getObject("npatlas_id");
            Integer npatlasId = npatlasIdObj == null ? null : ((Number) npatlasIdObj).intValue();

            Object fahfaIdObj = rs.getObject("fahfa_id");
            Integer fahfaId = fahfaIdObj == null ? null : ((Number) fahfaIdObj).intValue();

            Object ohPositionIdObj = rs.getObject("oh_position_id");
            Integer ohPositionId = ohPositionIdObj == null ? null : ((Number) ohPositionIdObj).intValue();

            GCMSCompound compound = GCMSCompound.builder()
                    .compoundId(rs.getInt("compound_id"))
                    .compoundName(rs.getString("compound_name"))
                    .mass(rs.getDouble("mass"))
                    .formula(rs.getString("formula"))
                    .formulaType(inferredFormulaType)
                    .logP(getNullableDouble(rs, "logP"))
                    .casId(rs.getString("cas_id"))
                    .chargeType(getNullableInteger(rs, "charge_type"))
                    .chargeNumber(getNullableInteger(rs, "charge_number"))
                    .compoundType(compoundType)
                    .inchi(rs.getString("inchi"))
                    .inchiKey(rs.getString("inchi_key"))
                    .smiles(rs.getString("smiles"))
                    .keggID(rs.getString("kegg_id"))
                    .lmID(rs.getString("lm_id"))
                    .hmdbID(rs.getString("hmdb_id"))
                    .agilentID(rs.getString("agilent_id"))
                    .pcID(pcId)
                    .chebiID(chebiId)
                    .inHouseID(rs.getString("in_house_id"))
                    .aspergillusID(aspergillusId)
                    .knapsackID(rs.getString("knapsack_id"))
                    .npatlasID(npatlasId)
                    .fahfaID(fahfaId)
                    .ohPositionID(ohPositionId)
                    .aspergillusWebName(rs.getString("aspergillus_web_name"))
                    .dbRI(rs.getDouble("RI"))
                    .derivatizationMethod(queryData.getDerivatizationMethod())
                    .gcColumn(queryData.getColumnType())
                    .GCMSSpectrum(new ArrayList<>())
                    .build();

            //CREATION OF THE Spectrum
            Spectrum spectrum = Spectrum.builder()
                    .spectrumId(rs.getInt("gcms_spectrum_id"))
                    .build();

            compound.getGCMSSpectrum().add(spectrum);

            return compound;
        };

        List<GCMSCompound> gcmsCompoundList = jdbcTemplate.query(sql1, params1, gcmsCompoundCustomMapper);

        return gcmsCompoundList;
    }

    /** Fetch each spectrum once, then attach peaks by ID in linear time. */
    private void getSpectrumInformation(List<GCMSCompound> compounds) {
        java.util.Set<Integer> ids = new java.util.LinkedHashSet<>();
        for (GCMSCompound compound : compounds) {
            for (Spectrum spectrum : compound.getGCMSSpectrum()) ids.add(spectrum.getSpectrumId());
        }
        if (ids.isEmpty()) return;
        java.util.Map<Integer, List<Peak>> peaks = new java.util.HashMap<>();
        for (var batch : JdbcBatches.partition(ids)) jdbcTemplate.query("""
                SELECT p.gcms_spectrum_id, p.mz, p.intensity
                FROM gcms_spectrum s JOIN gcms_peaks p ON s.gcms_spectrum_id = p.gcms_spectrum_id
                WHERE s.gcms_spectrum_id IN (:ids)
                """, java.util.Map.of("ids", batch), (org.springframework.jdbc.core.RowCallbackHandler) rs ->
                peaks.computeIfAbsent(rs.getInt("gcms_spectrum_id"), ignored -> new ArrayList<>())
                        .add(Peak.builder().mzValue(rs.getDouble("mz")).intensity(rs.getDouble("intensity")).build()));
        for (GCMSCompound compound : compounds) {
            for (Spectrum spectrum : compound.getGCMSSpectrum()) {
                spectrum.setSpectrum(new ArrayList<>(peaks.getOrDefault(spectrum.getSpectrumId(), List.of())));
            }
        }
    }

    /**
     * Creates the GCMSQueryResponseDTO from a GCMSCompound List (gcmsCompoundList)
     * @param gcmsCompoundList the list containning the found GCMSCompounds
     * @return a list with the information of gcmsCompoundList
     */
    private List<GCMSQueryResponseDTO> creationGCMSQueryResponseDTOFromgcmsCompoundList(List<GCMSCompound> gcmsCompoundList){
        List<GCMSQueryResponseDTO> infoAllRelevantCompounds = new ArrayList<>();
        GCMSQueryResponseDTO queryResponseDTO = new GCMSQueryResponseDTO();

        //COMPOUND
        int size = gcmsCompoundList.size();
        for(int i=0; i<size; i++){
            int compoundId = gcmsCompoundList.get(i).getCompoundId();
            String compoundName = gcmsCompoundList.get(i).getCompoundName();
            Double compoundMonoisotopicMass = gcmsCompoundList.get(i).getMass();
            String compoundFormula = gcmsCompoundList.get(i).getFormula();
            FormulaType formulaType = gcmsCompoundList.get(i).getFormulaType();
            Double logP = gcmsCompoundList.get(i).getLogP();
            String casId = gcmsCompoundList.get(i).getCasId();

            Integer chargeType = gcmsCompoundList.get(i).getChargeType();
            Integer chargeNumber = gcmsCompoundList.get(i).getChargeNumber();
            CompoundType compoundType = gcmsCompoundList.get(i).getCompoundType();

            String inchi = gcmsCompoundList.get(i).getInchi();
            String inchiKey = gcmsCompoundList.get(i).getInchiKey();
            String smiles = gcmsCompoundList.get(i).getSmiles();
            String keggID = gcmsCompoundList.get(i).getKeggID();
            String lmID = gcmsCompoundList.get(i).getLmID();
            String hmdbID = gcmsCompoundList.get(i).getHmdbID();
            String agilentID = gcmsCompoundList.get(i).getAgilentID();
            Integer pcID = gcmsCompoundList.get(i).getPcID();
            Integer chebiID = gcmsCompoundList.get(i).getChebiID();
            String inHouseID = gcmsCompoundList.get(i).getInHouseID();
            Integer aspergillusID = gcmsCompoundList.get(i).getAspergillusID();
            String knapsackID = gcmsCompoundList.get(i).getKnapsackID();
            Integer npatlasID = gcmsCompoundList.get(i).getNpatlasID();
            Integer fahfaID = gcmsCompoundList.get(i).getFahfaID();
            Integer ohPositionID = gcmsCompoundList.get(i).getOhPositionID();
            String aspergillusWebName = gcmsCompoundList.get(i).getAspergillusWebName();

            DerivatizationMethod derivatizationMethod = gcmsCompoundList.get(i).getDerivatizationMethod();
            ColumnType gcColumn = gcmsCompoundList.get(i).getGcColumn();

            double RI = gcmsCompoundList.get(i).getDbRI();

            //SPECTRUM
            int sizeSpectrum = gcmsCompoundList.get(i).getGCMSSpectrum().size();
            List<Spectrum> spectrumListCopy = new ArrayList<>();
            for (int j=0; j<sizeSpectrum; j++){
                Spectrum spectrum = gcmsCompoundList.get(i).getGCMSSpectrum().get(j);
                int spectrumId = spectrum.getSpectrumId();

                //PEAKS
                int sizePeak = spectrum.getSpectrum().size();
                List<Peak> peakListCopy = new ArrayList<>();
                for(int k=0; k<sizePeak; k++){
                    Peak peak = spectrum.getSpectrum().get(k);
                    double mz = peak.getMzValue();
                    double intensity = peak.getIntensity();
                    Peak peakCopy = Peak.builder().mzValue(mz).intensity(intensity).build();
                    peakListCopy.add(peakCopy);
                }
                Spectrum spectrumCopy = Spectrum.builder().spectrumId(spectrumId)
                        .spectrum(peakListCopy).build();
                spectrumListCopy.add(spectrumCopy);
            }

            queryResponseDTO = GCMSQueryResponseDTO.builder()
                    .compoundId(compoundId).compoundName(compoundName).monoisotopicMass(compoundMonoisotopicMass)
                    .formula(compoundFormula).formulaType(formulaType).logP(logP).casId(casId)
                    .charge_type(chargeType).charge_number(chargeNumber).compound_type(compoundType)
                    .inchi(inchi).inchiKey(inchiKey).smiles(smiles)
                    .keggID(keggID).lmID(lmID).hmdbID(hmdbID).agilentID(agilentID)
                    .pcID(pcID).chebiID(chebiID).inHouseID(inHouseID).aspergillusID(aspergillusID)
                    .knapsackID(knapsackID).npatlasID(npatlasID).fahfaID(fahfaID).ohPositionID(ohPositionID)
                    .aspergillusWebName(aspergillusWebName)
                    .dertype(derivatizationMethod).gcColumn(gcColumn)
                    .RI(RI).GCMSSpectrum(spectrumListCopy)
                    .build();
            infoAllRelevantCompounds.add(queryResponseDTO);
        }
        return  infoAllRelevantCompounds;
    }

    /**
     * From the information given by the user, it checks in the database for the matching compounds.
     * @param queryData information given by the user
     * @return a list with the matching compounds
     * @throws IOException
     */
    public List<GCMSQueryResponseDTO> findMatchingCompounds(GCMSFeatureQueryDTO queryData) throws IOException {
        List<GCMSQueryResponseDTO> infoAllRelevantCompounds = new ArrayList<>();

        List<GCMSCompound> gcmsCompoundList = getCompoundInformation(queryData);

        getSpectrumInformation(gcmsCompoundList);

        infoAllRelevantCompounds = creationGCMSQueryResponseDTOFromgcmsCompoundList(gcmsCompoundList);

        return infoAllRelevantCompounds;
    }


    private static Double getNullableDouble(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        Object value = rs.getObject(column);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.valueOf(value.toString());
    }

    private static Integer getNullableInteger(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        Number value = (Number) rs.getObject(column);
        return value == null ? null : Math.toIntExact(value.longValue());
    }

}
