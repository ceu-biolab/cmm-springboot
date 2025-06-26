package ceu.biolab.cmm.gcmsSearch.repository;

import ceu.biolab.cmm.gcmsSearch.domain.ColumnType;
import ceu.biolab.cmm.gcmsSearch.domain.DerivatizationMethod;
import ceu.biolab.cmm.gcmsSearch.domain.GCMSCompound;
import ceu.biolab.cmm.gcmsSearch.dto.GCMSFeatureQueryDTO;
import ceu.biolab.cmm.gcmsSearch.dto.GCMSQueryResponseDTO;
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
import java.nio.file.Files;
import java.nio.file.Paths;
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
     * With the information given by the user (queryData) and a query it gets the information from a database (compound_id, compound_name, mass, formula,
     * inchi, inchi_key, smiles, RT, RI & gcms_spectrum_id) & creates the GCMSCompound
     *
     * @param queryData  GCMSFeatureQueryDTO that contains the information that is needed for the search
     * @return gcmsCompoundList - a list of GCMSCompound with the information of the database
     * @throws IOException
     */
    private List<GCMSCompound> getFirstQuery (GCMSFeatureQueryDTO queryData) throws IOException {
        Resource resource1 = resourceLoader.getResource("classpath:sql/gcms_query1.sql");
        String sql1 = new String(Files.readAllBytes(Paths.get(resource1.getURI())));

        MapSqlParameterSource params1 = new MapSqlParameterSource();
        params1.addValue("RILower", queryData.getMinRI());
        params1.addValue("RIUpper", queryData.getMaxRI());
        //SINCE BOTH PARAMETERS ARE ENUMS I WILL USE .name() TO CONVERT THEM TO STRING
        params1.addValue("ColumnType", queryData.getColumnType().name());
        params1.addValue("DerivatizationType", queryData.getDerivatizationMethod().name());

        /*
        WITH THE QUERY AND THE GIVEN PARAMETERS, WE OBTAIN THE DATA WE ARE LOOKING FOR
        (COMPOUND, IDENTIFIERS, RI, RT, SPECTRUM ID)
        THEN THE VALUES ARE MAPPED TO THE CLASS GCMSCompound. spectrumId IS SAVED ON THE CLASS Spectrum
        THE RESULTING OBJECTS ARE SAVED ON A LIST OF COMPOUNDS
        */

        RowMapper<GCMSCompound> gcmsCompoundCustomMapper = (rs, rowNum) -> {
            //FROM THE DATA BASE I WILL OBTAIN compound_id, compound_name, mass, formula,
            //              inchi, inchi_key, smiles, RT, RI & gcms_spectrum_id
            //FROM queryData (I USED THEM TO FIND THE VALUES ON MY DATABASE SO THE VALUES ARE GOING TO BE CONSTANT
            // FOR EVERY COMPOUND) I OBTAIN THE derivatizationMethod & gcColumn

            //CREATION OF THE GCMSCompound
            GCMSCompound compound = GCMSCompound.builder()
                    .compoundId(rs.getInt("compound_id"))
                    .compoundName(rs.getString("compound_name"))
                    .mass(rs.getDouble("mass"))
                    .formula(rs.getString("formula"))
                    .inchi(rs.getString("inchi"))
                    .inchiKey(rs.getString("inchi_key"))
                    .smiles(rs.getString("smiles"))
                    .dbRI(rs.getDouble("RI"))
                    .dbRT(rs.getDouble("RT"))
                    .derivatizationMethod(queryData.getDerivatizationMethod())//.derivatizationMethod(DerivatizationMethod.valueOf(rs.getString("derivatization_method")))
                    .gcColumn(queryData.getColumnType())
                    .GCMSSpectrum(new ArrayList<>())
                    .build();

            //CREATION OF THE Spectrum (IT WILL ONLY CONTAIN THE Id) -> using SpectrumId
            Spectrum spectrum = Spectrum.builder()
                    .spectrumId(rs.getInt("gcms_spectrum_id"))
                    .build();

            //ADD THE SPECTRUM TO THE LIST
            compound.getGCMSSpectrum().add(spectrum);

            return compound;
        };

        //CREATE A LIST WITH ALL THE RECEIVED COMPOUNDS (GCMSCompound) USING AS TEMPLATE THE gcmsCompoundCustomMapper
        //INSTEAD OF BeanPropertyRowMapper<>(GCMSCompound.class) SINCE I HAVE A ATTRIBUTE (spectrumId) THAT IS INSIDE ANOTHER CLASS (Spectrum)
        List<GCMSCompound> gcmsCompoundList = jdbcTemplate.query(sql1, params1, gcmsCompoundCustomMapper);

        return gcmsCompoundList;
    }

    /**
     * From GCMSCompoundList, gets the ids of each of the spectrum of each compound
     * @param gcmsCompoundsList  list of GCMSCompound
     * @return listId - list of all spectrum id of all compounds from gcmsCompoundsList
     */
    private List<Integer> listAllSpectrumIdsCompounds (List<GCMSCompound> gcmsCompoundsList){
        List<Integer> listId = new ArrayList<>();

        int compoundListSize = gcmsCompoundsList.size();

        for(int i=0; i<compoundListSize; i++){ //ITERATES OVER THE COMPOUND LIST
            int spectrumListSize = gcmsCompoundsList.get(i).getGCMSSpectrum().size(); //Sice of each Spectra
            for(int j=0; j<spectrumListSize; j++){ //ITERATES OVER THE SPECTRUM LIST OF EACH COMPOUND
                int spectrumId = gcmsCompoundsList.get(i).getGCMSSpectrum().get(j).getSpectrumId(); //Gets the Id of aech Spectrum
                listId.add(spectrumId);
            }
        }
        return listId;
    }

    /**
     * Is the ResultSetExtractor, where for each spectrum_id, access the database and relates the information
     * with the classes Spectrum and Peak
     *
     * @param spectrumId is used for the creation of a Spectrum
     * @return spectrum with all its peaks
     */
    //private ResultSetExtractor<List<Spectrum>> SpectrumPeaksExtractor(int spectrumId) {
    private ResultSetExtractor<Spectrum> spectrumPeaksExtractor(int spectrumId) {
        return rs -> {
            //Map<Integer, Spectrum> spectrumMap = new LinkedHashMap<>(); -> I don't need it because I will do it for each spectrumid
            Spectrum spectrum = new Spectrum();
            while (rs.next()) {
                //int spectrumId = rs.getInt("spectrum_id");

                //IF spectrum DOES NOT HAVE THE DATA OF THE DATABASE IT WILL BE CREATED
                if(spectrum.getSpectrumId() == -1) {
                    spectrum = //spectrumMap.computeIfAbsent(spectrumId, id ->
                            Spectrum.builder()
                                    .compoundId(rs.getInt("compound_id"))
                                    .spectrumId(spectrumId)
                                    .spectrum(new ArrayList<>())
                                    .build();
                }
                //CREATION OF EACH Peak
                Peak peak = Peak.builder()
                        .mzValue(rs.getDouble("mz"))
                        .intensity(rs.getDouble("intensity"))
                        .build();
                //ADDITION OF EACH peak TO spectrum
                spectrum.getSpectrum().add(peak);
            }
            ///return new ArrayList<>(spectrumMap.values());
            //List<Spectrum> sptr = new ArrayList<>(spectrumMap.values());
            return spectrum;
        };
    }

    /**
     * Using the query and spectrum_id, looks for the Peaks of the spectrum in the database.
     * It uses a ResultSetExtractor: SpectrumPeaksExtractor(spectrumId)
     *
     * @param spectrumId
     * @return spectrum - Spectrum. For an id its spectrum
     * @throws IOException
     */
    private Spectrum spectrumWithPeaksFromDB(int spectrumId) throws IOException {
        Resource resource2 = resourceLoader.getResource("classpath:sql/gcms_query2.sql");
        String sql2 = new String(Files.readAllBytes(Paths.get(resource2.getURI())));

        MapSqlParameterSource params2 = new MapSqlParameterSource();
        params2.addValue("SpectrumId", spectrumId);

        //SINCE IM DOING A LOOP FOR EACH ID I WILL ONLY HAVE 1 SPECTRUM
        Spectrum spectrum = jdbcTemplate.query(sql2, params2, spectrumPeaksExtractor(spectrumId));
        return spectrum;
    }

    /**
     * For each of the spectrumId, using a query, looks for the Spectrum information (compound_id, list of Peaks)
     *
     * @param completeListSpectrumIds  Integer List that contains the spectrum id's
     * @return spectrumList - a list of Spectrum with the information of the database
     * @throws IOException
     */
    private List<Spectrum> getSecondQuery (List<Integer> completeListSpectrumIds) throws IOException {

        List<Spectrum> spectrumList = new ArrayList<>();

        //List<Integer> completeListSpectrumIds =  listAllSpectrumIdsCompounds(gcmsCompoundsList);
        int sizeIdList = completeListSpectrumIds.size();

        //Resource resource2 = resourceLoader.getResource("classpath:sql/gcms_query2.sql");
        //String sql2 = new String(Files.readAllBytes(Paths.get(resource2.getURI())));

        for(int i=0; i<sizeIdList; i++){ //ITERATES OVER completeListSpectrumIds
            int spectrumId = completeListSpectrumIds.get(i);

            //MapSqlParameterSource params2 = new MapSqlParameterSource();
            //params2.addValue("gcms_spectrum_id", spectrumId);

            Spectrum spectrum = spectrumWithPeaksFromDB(spectrumId);
            spectrumList.add(spectrum);

            // Crear funcion para -> asociar spectrum al objeto correspondiente -> hacerlo en la principal

        }

        return spectrumList;

        /*ResultSetExtractor<List<Spectrum>> extractor = rs -> {
            Map<Integer, Spectrum> spectrumMap = new LinkedHashMap<>();

            while (rs.next()) {
                int spectrumId = rs.getInt("spectrum_id");

                // Creamos o recuperamos el Spectrum
                Spectrum spectrum = spectrumMap.computeIfAbsent(spectrumId, id ->
                        Spectrum.builder()
                                .spectrumId(id)
                                .peaks(new ArrayList<>())
                                .build()
                );

                // Creamos el peak del resultSet
                Peak peak = Peak.builder()
                        .mz(rs.getDouble("mz"))
                        .intensity(rs.getDouble("intensity"))
                        .build();

                // Lo agregamos al Spectrum correspondiente
                spectrum.getPeaks().add(peak);
            }

            return new ArrayList<>(spectrumMap.values());
        };

        List<Spectrum> spectrumList = jdbcTemplate.query(
            "SELECT spectrum_id, mz, intensity FROM peaks WHERE spectrum_id IN (:ids)",
            new MapSqlParameterSource("ids", spectrumIds), // Lista con los ids
            extractor
        );*/

    }

    /**
     * Match each GCMSCompound with its Spectrum using the compound id
     *
     * @param gcmsCompoundList GCMSCompoun List - The Spectrum only has the id
     * @param spectrumWithPeaksList Spectrum with its Peak List
     */
    private void joinCompoundSpectrum (List<GCMSCompound> gcmsCompoundList, List<Spectrum> spectrumWithPeaksList){
        int sizeCompoundList = gcmsCompoundList.size();
        int sizeSpectrumPeakList = spectrumWithPeaksList.size();
        Spectrum spectrum;

        //COMPARES THE LIST TO KNOW WHICH ONE IS THE LARGEST SO THAT THE ASSOCIATION BETWEEN BOTH LISTS CAN BE MADE
        for(int i=0; i<sizeCompoundList; i++) { //ITERATES OVER gcmsCompoundList
            int idCompoundFromCompound = gcmsCompoundList.get(i).getCompoundId();
            for (int j = 0; j < sizeSpectrumPeakList; j++) {//ITERATES OVER spectrumWithPeaksList
                int idCompoundFromSpectrum = spectrumWithPeaksList.get(j).getCompoundId();
                if (idCompoundFromCompound == idCompoundFromSpectrum) {
                    spectrum = spectrumWithPeaksList.get(j); //Spectrum FROM spectrumWithPeaksList
                    //gcmsCompoundList.get(i).getGCMSSpectrum().add(spectrum);
                    int tam = gcmsCompoundList.get(i).getGCMSSpectrum().size();
                    for (int k = 0; k < tam; k++) {
                        if (gcmsCompoundList.get(i).getGCMSSpectrum().get(k).getSpectrumId() == idCompoundFromSpectrum) {
                            gcmsCompoundList.get(i).getGCMSSpectrum().get(k).setSpectrum(spectrum.getSpectrum());
                        }
                    }
                }
            }
        }
    }
    /*INCORRECCTO
    private void joinCompoundSpectrum (List<GCMSCompound> gcmsCompoundList, List<Spectrum> spectrumWithPeaksList){
        int sizeCompoundList = gcmsCompoundList.size();
        int sizeSpectrumPeakList = spectrumWithPeaksList.size();
        int largestList;
        int shortestList;
        Spectrum spectrum;

        //COMPARES THE LIST TO KNOW WHICH ONE IS THE LARGEST SO THAT THE ASSOCIATION BETWEEN BOTH LISTS CAN BE MADE
        if(sizeCompoundList>=sizeSpectrumPeakList){
            largestList = sizeCompoundList;
            shortestList = sizeSpectrumPeakList;

            for(int i=0; i<largestList; i++){ //ITERATES OVER gcmsCompoundList
                int idCompoundFromCompound = gcmsCompoundList.get(i).getCompoundId();
                for(int j=0; j<shortestList; j++){//ITERATES OVER spectrumWithPeaksList
                    int idCompoundFromSpectrum = spectrumWithPeaksList.get(j).getCompoundId();
                    if(idCompoundFromCompound==idCompoundFromSpectrum){
                        spectrum = spectrumWithPeaksList.get(j); //Spectrum FROM spectrumWithPeaksList
                        //gcmsCompoundList.get(i).getGCMSSpectrum().add(spectrum);
                        int tam = gcmsCompoundList.get(i).getGCMSSpectrum().size();
                        for(int k=0; k<tam; k++){
                            if(gcmsCompoundList.get(i).getGCMSSpectrum().get(k).getSpectrumId() == idCompoundFromSpectrum){
                                gcmsCompoundList.get(i).getGCMSSpectrum().get(k).setSpectrum(spectrum.getSpectrum());
                            }
                        }
                    }
                }
            }
        } else{
            largestList = sizeSpectrumPeakList;
            shortestList = sizeCompoundList;
            for(int i=0; i<largestList; i++){ //ITERATES OVER spectrumWithPeaksList
                int idCompoundFromSpectrum = spectrumWithPeaksList.get(i).getCompoundId();
                for(int j=0; j<shortestList; j++){//ITERATES OVER gcmsCompoundList
                    int idCompoundFromCompound = gcmsCompoundList.get(j).getCompoundId();
                    if(idCompoundFromCompound==idCompoundFromSpectrum){
                        spectrum = spectrumWithPeaksList.get(i); //Spectrum FROM spectrumWithPeaksList
                        //gcmsCompoundList.get(j).getGCMSSpectrum().add(spectrum);
                        int tam = gcmsCompoundList.get(j).getGCMSSpectrum().size();
                        for(int k=0; k<tam; k++){
                            if(gcmsCompoundList.get(j).getGCMSSpectrum().get(k).getSpectrumId() == idCompoundFromSpectrum){
                                gcmsCompoundList.get(j).getGCMSSpectrum().get(k).setSpectrum(spectrum.getSpectrum());
                            }
                        }
                    }
                }
            }
        }
    }*/

    /**
     * Creates the GCMSQueryResponseDTO from a GCMSCompound List (gcmsCompoundList)
     * @param gcmsCompoundList Relevant GCMSCompounds grouped in a GCMSCompound list
     * @return GCMSQueryResponseDTO List with the information of gcmsCompoundList
     */
    private List<GCMSQueryResponseDTO> creationGCMSQueryResponseDTOFromgcmsCompoundList(List<GCMSCompound> gcmsCompoundList){
        List<GCMSQueryResponseDTO> infoAllRelevantCompounds = new ArrayList<>();
        GCMSQueryResponseDTO queryResponseDTO = new GCMSQueryResponseDTO();
        //List<Spectrum> spectrumListCopy = new ArrayList<>();
        //List<Peak> peakListCopy = new ArrayList<>();

        //COMPOUND
        int size = gcmsCompoundList.size();
        for(int i=0; i<size; i++){
            int compoundId = gcmsCompoundList.get(i).getCompoundId();
            String compoundName = gcmsCompoundList.get(i).getCompoundName();
            double compoundMonoisotopicMass = gcmsCompoundList.get(i).getMass();
            String compoundFormula = gcmsCompoundList.get(i).getFormula();

            DerivatizationMethod derivatizationMethod = gcmsCompoundList.get(i).getDerivatizationMethod();
            ColumnType gcColumn = gcmsCompoundList.get(i).getGcColumn();

            double RI = gcmsCompoundList.get(i).getDbRI();
            double RT = gcmsCompoundList.get(i).getDbRT();

            //SPECTRUM
            int sizeSpectrum = gcmsCompoundList.get(i).getGCMSSpectrum().size();
            List<Spectrum> spectrumListCopy = new ArrayList<>();
            for (int j=0; j<sizeSpectrum; j++){
                Spectrum spectrum = gcmsCompoundList.get(i).getGCMSSpectrum().get(j);
                int spectrumId = spectrum.getSpectrumId();
                int compoundIdSpectrum = spectrum.getCompoundId();

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
                        .compoundId(compoundIdSpectrum).spectrum(peakListCopy).build();
                spectrumListCopy.add(spectrumCopy);
            }
            queryResponseDTO = GCMSQueryResponseDTO.builder()
                    .compoundId(compoundId).compoundName(compoundName).monoisotopicMass(compoundMonoisotopicMass)
                    .formula(compoundFormula).dertype(derivatizationMethod).gcColumn(gcColumn)
                    .RI(RI).RT(RT).GCMSSpectrum(spectrumListCopy)
                    .build();
            infoAllRelevantCompounds.add(queryResponseDTO);
        }
        return  infoAllRelevantCompounds;
    }

    public List<GCMSQueryResponseDTO> findMatchingCompounds(GCMSFeatureQueryDTO queryData) throws IOException {
        List<GCMSQueryResponseDTO> infoAllRelevantCompounds = new ArrayList<>();

        List<GCMSCompound> gcmsCompoundList = getFirstQuery(queryData);

        List<Integer> completeListSpectrumIds =  listAllSpectrumIdsCompounds(gcmsCompoundList);

        //GETS SPECTRUM LIST WITH THE PEAKS
        List<Spectrum> spectrumWithPeakList = getSecondQuery(completeListSpectrumIds);

        //JOIN EACH GCMSCompound WITH THE CORRECT Spectrum
        joinCompoundSpectrum(gcmsCompoundList, spectrumWithPeakList);

        infoAllRelevantCompounds = creationGCMSQueryResponseDTOFromgcmsCompoundList(gcmsCompoundList);

        return infoAllRelevantCompounds;
    }

    /*public List<GCMSQueryResponseDTO> findMatchingCompounds(GCMSFeatureQueryDTO queryData) throws IOException {
        //TO OBTAIN THE PAIRS OF IDS
        Resource resource1 = resourceLoader.getResource("classpath:sql/gcms_query_ids.sql");
        String sql1 = new String(Files.readAllBytes(Paths.get(resource1.getURI())));

        MapSqlParameterSource params1 = new MapSqlParameterSource();
        params1.addValue("RILower", queryData.getMinRI());
        params1.addValue("RIUpper", queryData.getMaxRI());
        //SINCE BOTH PARAMS ARE ENUMS I WILL USE .name() TO CONVERT IT TO STRING
        params1.addValue("ColumnType", queryData.getColumnType().name());
        params1.addValue("DerivatizationType", queryData.getDerivatizationMethod().name());

        /*
        WITH THE QUERY AND THE GIVEN PARAMETERS, WE OBTAIN THE DATA WE ARE LOOKING FOR (IDs)
        THEN THE VALUES ARE MAPPED TO THE CLASS THAT CONTAINS THE PAIRS (compoundId & DerivatizationMethodId)
        THE RESULTING OBJECTS ARE SAVED ON A LIST
         *
        List<CompoundDerivatizationPairIds> pairIdsList = jdbcTemplate.query(sql1, params1, new BeanPropertyRowMapper<>(CompoundDerivatizationPairIds.class));

        //TO OBTAIN ALL THE INFORMATION
        Resource resource2 = resourceLoader.getResource("classpath:sql/gcms_query_InformationAll.sql");
        String sql2 = new String(Files.readAllBytes(Paths.get(resource2.getURI())));

        MapSqlParameterSource params2;// = new MapSqlParameterSource();

        int i;
        int j;
        int idcompound;
        List<GCMSQueryResponseDTO> infoAllRelevantCompounds = new ArrayList<>();
        Map<Integer, GCMSQueryResponseDTO> infoOneCompoundGrouped = new LinkedHashMap<>();

        //ITERATE OVER THE LIST WITH THE IDS
        for(i=0; i<pairIdsList.size(); i++){
            params2 = new MapSqlParameterSource(); //IT WILL RESET EVERY ITERATION
            params2.addValue("CompoundId", pairIdsList.get(i).getCompoundId());
            params2.addValue("DerivatizationMethodId", pairIdsList.get(i).getDerivatizationMethodId());

            //A LIST WITH THE VALUES OF THE SAME COMPOUND -> SAME INFORMATION EXCEPT MZ&INTENSITY (SPECTRUM)
            List<GCMSCompound> infoOneCompound = jdbcTemplate.query(sql2, params2, new BeanPropertyRowMapper<>(GCMSCompound.class));

            // List<Spectrum> GetSpectrumFromCompoundIDAndDerivatizationID
            // gcmsCompound.addSpectrum(spectrum correspondiente de la query).

            //ITERATES OVER THE LIST WITH THE SAME COMPOUND DIFFERENT PEAKS
            for(j=0; j<infoOneCompound.size(); j++){
                idcompound = infoOneCompound.get(j).getCompoundId();

                GCMSQueryResponseDTO qrdto = infoOneCompoundGrouped.get(idcompound);

                //IF THE COMPOUND IS NOT AGRUPATED
                if(qrdto == null){
                    qrdto = new GCMSQueryResponseDTO(); //Inicialice all the values
                    qrdto.setCompoundId(idcompound);

                    qrdto.setCompoundName(infoOneCompound.get(j).getCompoundName());
                    qrdto.setMonoisotopicMass(infoOneCompound.get(j).getMass());
                    qrdto.setFormula(infoOneCompound.get(j).getFormula());
                    //TODO Los necesito???
                    //qrdto.setFormulaType(infoOneCompound.get(j).getFormulaType());
                    //qrdto.setCompoundType(infoOneCompound.get(j).getCompoundType());
                    //qrdto.setLogP(infoOneCompound.get(j).getLogP());

                    qrdto.setDertype(infoOneCompound.get(j).getDerivatizationMethod());
                    qrdto.setGcColumn(infoOneCompound.get(j).getGcColumn());

                    Spectrum spectrum = new Spectrum();
                    qrdto.getGCMSSpectrum().add(spectrum);
                    infoOneCompoundGrouped.put(idcompound,qrdto);
                }

                double mz = infoOneCompound.get(j).getMz();
                double intensity = infoOneCompound.get(j).getIntensity();
                Peak peak = new Peak(mz, intensity);

                qrdto.getGCMSSpectrum().get(0).getSpectrum().add(peak); //SINCE I ONLY HAVE 1 SPECTRUM/COMPOUND/DERIVATIZATION

            }
        }
        infoAllRelevantCompounds = new ArrayList<>(infoOneCompoundGrouped.values());

        return infoAllRelevantCompounds;
    }*/
}
