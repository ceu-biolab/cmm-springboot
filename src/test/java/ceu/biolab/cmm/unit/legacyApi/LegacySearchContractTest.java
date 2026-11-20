package ceu.biolab.cmm.unit.legacyApi;

import ceu.biolab.cmm.legacyApi.controller.*;
import ceu.biolab.cmm.legacyApi.dto.*;
import ceu.biolab.cmm.legacyApi.mapper.*;
import ceu.biolab.cmm.legacyApi.service.*;
import ceu.biolab.cmm.config.GlobalExceptionHandler;
import ceu.biolab.cmm.msSearch.service.CompoundService;
import ceu.biolab.cmm.msSearch.dto.*;
import ceu.biolab.cmm.browseSearch.service.BrowseSearchService;
import ceu.biolab.cmm.browseSearch.dto.*;
import ceu.biolab.cmm.lcmsSearch.service.BatchAdvancedSearchService;
import ceu.biolab.cmm.lcmsSearch.dto.BatchAdvancedSearchRequestDTO;
import ceu.biolab.cmm.MSMSSearch.service.MSMSSearchService;
import ceu.biolab.cmm.MSMSSearch.dto.*;
import ceu.biolab.cmm.MSMSSearch.domain.*;
import ceu.biolab.cmm.CEMSSearch.service.*;
import ceu.biolab.cmm.CEMSSearch.dto.*;
import ceu.biolab.cmm.CEMSMarkers.service.*;
import ceu.biolab.cmm.CEMSMarkers.dto.*;
import ceu.biolab.cmm.shared.domain.*;
import ceu.biolab.cmm.shared.domain.compound.*;
import ceu.biolab.cmm.shared.domain.msFeature.*;
import com.fasterxml.jackson.databind.*;
import jakarta.validation.Validator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;
import java.util.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({LegacyMsSearchController.class, LegacyCeSearchController.class})
@Import({GlobalExceptionHandler.class, LegacyOptionsMapper.class, LegacyResponseMapper.class,
        LegacyMsSearchService.class, LegacyAdvancedSearchService.class, LegacyCeRequestMapper.class,
        LegacyCeResponseMapper.class, LegacyCeSearchService.class})
class LegacySearchContractTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired Validator validator;
    @MockBean CompoundService compounds;
    @MockBean BrowseSearchService browse;
    @MockBean BatchAdvancedSearchService lcms;
    @MockBean MSMSSearchService msms;
    @MockBean CemsSearchService ce;
    @MockBean CemsRmtSearchService rmt;
    @MockBean Cems1MarkerService mt1;
    @MockBean Cems2MarkerService mt2;
    @MockBean CemsRmt1MarkerService rmt1;
    @MockBean CemsRmt2MarkerService rmt2;

    static final String OPTIONS = "\"tolerance\":10,\"ion_mode\":\"positive\",\"adducts\":[\"M+H\"],\"databases\":[\"hmdb\"]";
    static final String ADVANCED = "{" + OPTIONS + ",\"masses\":[101],\"retention_times\":[2],\"composite_spectra\":[[{\"mz\":101,\"intensity\":10}]]}";
    static final String CE = "{" + OPTIONS + ",\"masses\":[101],\"effective_mobilities\":[10],\"rmt\":[1],\"mt\":[2],"
            + "\"eff_mob_tolerance\":10,\"eff_mob_tolerance_mode\":\"percentage\",\"rmt_tolerance\":10,\"rmt_tolerance_mode\":\"percentage\","
            + "\"mt_tolerance\":10,\"mt_tolerance_mode\":\"percentage\",\"buffer\":\"formic-acid-1m-20c\",\"polarity\":\"direct\","
            + "\"rmt_reference\":\"l-methionine-sulfone\",\"marker\":\"L-Methionine sulfone\",\"marker_time\":2,"
            + "\"marker2\":\"Paracetamol\",\"marker2_time\":3,\"capillary_length\":1000,\"capillary_voltage\":30}";
    static final String MSMS = "{\"ion_mass\":101,\"ms_ms_peaks\":[{\"mz\":50,\"intensity\":10}]}";
    static Stream<Arguments> routes() {
        return Stream.of(
                Arguments.of("/simplesearch", "{\"mass\":101}"),
                Arguments.of("/browsesearch", "{\"name\":\"choline\"}"),
                Arguments.of("/msmssearch", MSMS),
                Arguments.of("/advancedsearch", "{\"mass\":101,\"retention_time\":2}"),
                Arguments.of("/advancedbatch", ADVANCED), Arguments.of("/v3/batch", "{\"masses\":[101]}"),
                Arguments.of("/v3/advancedbatch", ADVANCED),
                Arguments.of("/v3/cesearch/effmob", CE), Arguments.of("/v3/cesearch/exprmt", CE),
                Arguments.of("/v3/cesearch/mt1marker", CE), Arguments.of("/v3/cesearch/mt2marker", CE),
                Arguments.of("/v3/cesearch/rmt1marker", CE), Arguments.of("/v3/cesearch/rmt2marker", CE));
    }
    @BeforeEach void responses() {
        when(compounds.findCompoundsByMz(any())).thenReturn(new RTSearchResponseDTO());
        when(browse.search(any())).thenReturn(new BrowseQueryResponse());
        when(lcms.annotateAndScoreCmpoundsByMz(any())).thenReturn(List.of());
        when(msms.search(any())).thenReturn(new MSMSSearchResponseDTO());
        var result = new CemsSearchResponseDTO();
        var feature = new CeFeatureAnnotationsDTO();
        feature.setFeature(CeFeatureDTO.builder().mzValue(101).effectiveMobility(10).build());
        result.addFeature(feature);
        when(ce.search(any())).thenReturn(result); when(rmt.search(any())).thenReturn(result);
        when(mt1.search(any())).thenReturn(result); when(mt2.search(any())).thenReturn(result);
        when(rmt1.search(any())).thenReturn(result); when(rmt2.search(any())).thenReturn(result);
    }
    ResultActions request(String route, String payload) throws Exception {
        return mvc.perform(post("/api" + route).contentType(MediaType.APPLICATION_JSON).content(payload));
    }
    @ParameterizedTest @MethodSource("routes") void allThirteenRoutesKeepLegacyEnvelopes(String route, String body) throws Exception {
        var action = request(route, body).andExpect(status().isOk());
        if (route.contains("cesearch")) action.andExpect(jsonPath("$.compounds_found").isArray()).andExpect(jsonPath("$.fragments").isEmpty());
        else action.andExpect(content().json("{\"results\":[]}", true));
    }
    @ParameterizedTest @MethodSource("routes") void missingInputsFailBeforeAnyBackendCall(String route, String body) throws Exception {
        request(route, "{}").andExpect(status().isBadRequest());
        verifyNoInteractions(compounds, browse, lcms, msms, ce, rmt, mt1, mt2, rmt1, rmt2);
    }
    @ParameterizedTest @ValueSource(strings={"{\"mass\":101,\"databases\":null}","{\"mass\":101,\"databases\":[null]}",
            "{\"mass\":101,\"adducts\":[\"M+H\",\"garbage\"]}","{\"mass\":101,\"databases\":[\"mine\"]}",
            "{\"mass\":101,\"ion_mode\":\"neutral\"}","{\"mass\":1e999}","{\"mass\":101,\"tolerance\":1e999}",
            "{\"mass\":101,\"tolerance\":0}","{\"mass\":101,\"adducts\":null}"})
    void invalidSimplePayloadsAre400(String body) throws Exception {
        request("/simplesearch",body).andExpect(status().isBadRequest()); verifyNoInteractions(compounds);
    }
    @Test void minimalNegativeDefaultsUseNegativeCatalogAndCurrentDtoConstraints() throws Exception {
        request("/simplesearch", "{\"mass\":101,\"ion_mode\":\"negative\",\"masses_mode\":\"neutral\"}").andExpect(status().isOk());
        var captor = ArgumentCaptor.forClass(CompoundSimpleSearchRequestDTO.class); verify(compounds).findCompoundsByMz(captor.capture());
        var forwarded = captor.getValue();
        assertEquals(101 - Constants.PROTON_WEIGTH, forwarded.getMz(), 1e-10);
        assertTrue(forwarded.getAdductsString().contains("[M-H]-")); assertFalse(forwarded.getAdductsString().contains("[M+H]+"));
        assertTrue(validator.validate(forwarded).isEmpty());
    }
    @Test void browsePreservesLipidAndExactNameFilters() throws Exception {
        request("/browsesearch", "{\"name\":\"choline\",\"metabolites_type\":\"only-lipids\",\"exactName\":true,\"databases\":[\"lipidmaps\"]}").andExpect(status().isOk());
        var captor = ArgumentCaptor.forClass(BrowseSearchRequest.class); verify(browse).search(captor.capture());
        assertEquals(MetaboliteType.ONLYLIPIDS,captor.getValue().getMetaboliteType());
        assertEquals(Set.of(Database.LIPIDMAPS),captor.getValue().getDatabases()); assertTrue(captor.getValue().isExactName());
    }
    @ParameterizedTest @CsvSource({"experimental,EXPERIMENTAL","predicted,PREDICTED","'experimental,predicted',ALL"})
    void msmsSourceAndLegacyVoltageReachCurrentService(String source, SpectrumSource expected) throws Exception {
        var body = (com.fasterxml.jackson.databind.node.ObjectNode)json.readTree(MSMS);
        body.put("ionization_voltage","med"); var list = body.putArray("spectra_types"); for(String v:source.split(",")) list.add(v);
        request("/msmssearch",json.writeValueAsString(body)).andExpect(status().isOk());
        var captor = ArgumentCaptor.forClass(MSMSSearchRequestDTO.class); verify(msms).search(captor.capture());
        assertEquals(expected,captor.getValue().getSpectrumSource()); assertEquals(CIDEnergy.MED,captor.getValue().getCIDEnergy());
        assertTrue(validator.validate(captor.getValue()).isEmpty());
    }
    @Test void nestedInvalidPeaksNeverReachMsms() throws Exception {
        request("/msmssearch", MSMS.replace("\"mz\":50", "\"mz\":-1")).andExpect(status().isBadRequest());
        verifyNoInteractions(msms);
    }
    @Test void ceBufferReferenceAndMassModesAreTranslatedAndValidated() throws Exception {
        request("/v3/cesearch/exprmt", CE.replace("formic-acid-1m-20c","acetic-acid-1m-25c").replace("\"masses\":[101]","\"masses_mode\":\"neutral\",\"masses\":[101]")).andExpect(status().isOk());
        var captor = ArgumentCaptor.forClass(CemsRmtSearchRequestDTO.class);verify(rmt).search(captor.capture());
        var value=captor.getValue(); assertEquals("ACETIC_ACID_10PERCENT",value.getBufferCode());assertEquals(25d,value.getTemperature());
        assertEquals("L-Methionine sulfone",value.getRmtReference());assertEquals(101+Constants.PROTON_WEIGTH,value.getMasses().getFirst(),1e-10);
        assertTrue(validator.validate(value).isEmpty());
    }
    @Test void ceMissingCoordinateIs400() throws Exception {
        request("/v3/cesearch/effmob", CE.replace("\"effective_mobilities\":[10],", "")).andExpect(status().isBadRequest());verifyNoInteractions(ce);
    }
    @Test void advancedMapsBothListsAndConvertsNeutralMassOnce() throws Exception {
        var body=(com.fasterxml.jackson.databind.node.ObjectNode)json.readTree(ADVANCED);
        body.put("masses_mode","neutral"); body.putArray("all_masses").add(201);body.putArray("all_retention_times").add(3);
        body.putArray("all_composite_spectra").addArray().addObject().put("mz",201).put("intensity",10);
        request("/advancedbatch",json.writeValueAsString(body)).andExpect(status().isOk());
        var captor=ArgumentCaptor.forClass(BatchAdvancedSearchRequestDTO.class);verify(lcms).annotateAndScoreCmpoundsByMz(captor.capture());
        assertEquals(List.of(101+Constants.PROTON_WEIGTH,201+Constants.PROTON_WEIGTH),captor.getValue().getMz());
        assertEquals(List.of(2d,3d),captor.getValue().getRetentionTimes());assertTrue(validator.validate(captor.getValue()).isEmpty());
    }
    @Test void advancedMisalignedListsAre400WithoutSearch() throws Exception {
        request("/advancedbatch",ADVANCED.replace("\"retention_times\":[2]","\"retention_times\":[]")).andExpect(status().isBadRequest());verifyNoInteractions(lcms,compounds);
    }
    @Test void spectrumFreeAdvancedSearchFallsBackWithoutInventingPeaks() throws Exception {
        request("/advancedsearch","{\"mass\":101,\"retention_time\":2,\"masses_mode\":\"neutral\"}").andExpect(status().isOk());
        var captor=ArgumentCaptor.forClass(CompoundSimpleSearchRequestDTO.class);verify(compounds).findCompoundsByMz(captor.capture());
        assertEquals(101+Constants.PROTON_WEIGTH,captor.getValue().getMz(),1e-10);verifyNoInteractions(lcms);
    }
    @ParameterizedTest @CsvSource({"/v3/advancedbatch,2,1", "/advancedbatch,3,3"})
    void backgroundListsPreserveVersionSpecificSelection(String route,int forwardedCount,int returnedCount) throws Exception {
        var body=(com.fasterxml.jackson.databind.node.ObjectNode)json.readTree(ADVANCED);
        body.putArray("all_masses").add(101).add(201);body.putArray("all_retention_times").add(2).add(3);
        var spectra=body.putArray("all_composite_spectra");
        spectra.addArray().addObject().put("mz",101).put("intensity",10);
        spectra.addArray().addObject().put("mz",201).put("intensity",10);
        when(lcms.annotateAndScoreCmpoundsByMz(any())).thenAnswer(call -> {
            BatchAdvancedSearchRequestDTO request=call.getArgument(0);
            List<AnnotatedFeature> features=new ArrayList<>();
            for(int j=0;j<request.getMz().size();j++)features.add(LegacyResponseMapperTest.feature(request.getMz().get(j),request.getRetentionTimes().get(j),"[M+H]+"));
            return features;
        });
        request(route,json.writeValueAsString(body)).andExpect(status().isOk()).andExpect(jsonPath("$.results.length()").value(returnedCount));
        var captor=ArgumentCaptor.forClass(BatchAdvancedSearchRequestDTO.class);verify(lcms).annotateAndScoreCmpoundsByMz(captor.capture());
        assertEquals(forwardedCount,captor.getValue().getMz().size());
    }
    @ParameterizedTest @ValueSource(strings={"mt1marker","mt2marker","rmt1marker","rmt2marker"})
    void missingCalibrationDataIs400BeforeMarkerLookup(String route) throws Exception {
        request("/v3/cesearch/"+route,CE.replace("\"marker_time\":2,", "")).andExpect(status().isBadRequest());
        verifyNoInteractions(mt1,mt2,rmt1,rmt2);
    }
}
