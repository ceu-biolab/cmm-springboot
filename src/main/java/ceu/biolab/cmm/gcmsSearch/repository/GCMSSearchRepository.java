package ceu.biolab.cmm.gcmsSearch.repository;

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
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Repository
public class GCMSSearchRepository {
    private NamedParameterJdbcTemplate jdbcTemplate;
    private ResourceLoader resourceLoader;

    @Autowired
    public GCMSSearchRepository(NamedParameterJdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
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
        Resource resource1 = resourceLoader.getResource("classpath:sql/gcms_compound_information.sql");
        String sql1 = loadSql(resource1);

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

            GCMSCompound compound = GCMSCompound.builder()
                    .compoundId(rs.getInt("compound_id"))
                    .compoundName(rs.getString("compound_name"))
                    .mass(rs.getDouble("mass"))
                    .formula(rs.getString("formula"))
                    .formulaType(inferredFormulaType)
                    .logP(rs.getDouble("logP"))
                    .casId(rs.getString("cas_id"))
                    .chargeType(rs.getInt("charge_type"))
                    .chargeNumber(rs.getInt("charge_number"))
                    .compoundType(compoundType)
                    .inchi(rs.getString("inchi"))
                    .inchiKey(rs.getString("inchi_key"))
                    .smiles(rs.getString("smiles"))
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

    /**
     * From a list of GCMSCompound, gets the ids of each spectrum of the compounds
     * @param gcmsCompoundsList  list of GCMSCompound
     * @return a list with all the spectrum ids of the compounds
     */
    private List<Integer> listAllSpectrumIdsCompounds (List<GCMSCompound> gcmsCompoundsList){
        List<Integer> listId = new ArrayList<>();

        int compoundListSize = gcmsCompoundsList.size();

        for(int i=0; i<compoundListSize; i++){
            int spectrumListSize = gcmsCompoundsList.get(i).getGCMSSpectrum().size(); //Size of the Spectra
            for(int j=0; j<spectrumListSize; j++){
                int spectrumId = gcmsCompoundsList.get(i).getGCMSSpectrum().get(j).getSpectrumId(); //Gets the id of each Spectrum
                listId.add(spectrumId);
            }
        }
        return listId;
    }

    /**
     * It is a ResultSetExtractor, that for each spectrum id, it accesses the database and
     * stores the information in the Spectrum and Peak
     *
     * @param spectrumId is the database spectrum id and is used for the creation of a Spectrum
     * @return a spectrum with all its peaks
     */
    private ResultSetExtractor<Spectrum> spectrumPeaksExtractor(int spectrumId) {
        return rs -> {
            Spectrum spectrum = new Spectrum();
            while (rs.next()) {

                //IF spectrum DOES NOT HAVE THE DATA OF THE DATABASE IT WILL BE CREATED
                if(spectrum.getSpectrumId() == -1) {
                    spectrum = Spectrum.builder()
                                .spectrumId(spectrumId)
                                .spectrum(new ArrayList<>())
                                .build();
                }
                //CREATION OF EACH Peak
                Peak peak = Peak.builder()
                        .mzValue(rs.getDouble("mz"))
                        .intensity(rs.getDouble("intensity"))
                        .build();
                //ADDS EACH peak TO spectrum
                spectrum.getSpectrum().add(peak);
            }
            return spectrum;
        };
    }

    /**
     * Using a query and the spectrum id, it searches for the Peaks of the spectrum in the database.
     *
     * @param spectrumId
     * @return the Spectrum of an id
     * @throws IOException
     */
    private Spectrum spectrumWithPeaksFromDB(int spectrumId) throws IOException {
        Resource resource2 = resourceLoader.getResource("classpath:sql/gcms_spectrum_information.sql");
        String sql2 = loadSql(resource2);

        MapSqlParameterSource params2 = new MapSqlParameterSource();
        params2.addValue("SpectrumId", spectrumId);

        Spectrum spectrum = jdbcTemplate.query(sql2, params2, spectrumPeaksExtractor(spectrumId));
        return spectrum;
    }

    /**
     * For each of the spectrumId, using a query, searches for the Spectrum information (list of Peaks).
     * Then assigns the spectrum to the compound with the same spectrumId
     *
     * @param gcmsCompoundList  GCMSCompound List that contains GCMSCompound information
     * @throws IOException
     */
    private void getSpectrumInformation(List<GCMSCompound> gcmsCompoundList) throws IOException {

        List<Integer> completeListSpectrumIds =  listAllSpectrumIdsCompounds(gcmsCompoundList);
        int sizeIdList = completeListSpectrumIds.size();
        int sizeCompoundList = gcmsCompoundList.size();

        for(int i=0; i<sizeIdList; i++){
            int spectrumId = completeListSpectrumIds.get(i);

            Spectrum spectrum = spectrumWithPeaksFromDB(spectrumId);

            for(int j=0; j<sizeCompoundList; j++){
                int sizeGCMSSpectrumList = gcmsCompoundList.get(j).getGCMSSpectrum().size();
                for(int k=0; k<sizeGCMSSpectrumList; k++){
                    int spectrumIdFromCompound = gcmsCompoundList.get(j).getGCMSSpectrum().get(k).getSpectrumId();

                    if(spectrumId == spectrumIdFromCompound){
                        gcmsCompoundList.get(j).getGCMSSpectrum().get(k).setSpectrum(spectrum.getSpectrum());
                    }
                }
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

            int chargeType = gcmsCompoundList.get(i).getChargeType();
            int chargeNumber = gcmsCompoundList.get(i).getChargeNumber();
            CompoundType compoundType = gcmsCompoundList.get(i).getCompoundType();

            String inchi = gcmsCompoundList.get(i).getInchi();
            String inchiKey = gcmsCompoundList.get(i).getInchiKey();
            String smiles = gcmsCompoundList.get(i).getSmiles();

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

    private String loadSql(Resource resource) throws IOException {
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

}
