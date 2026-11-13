package ceu.biolab.cmm.unit.legacyApi;

import ceu.biolab.cmm.compoundDetails.dto.*;
import ceu.biolab.cmm.compoundDetails.repository.CompoundDetailsRepository;
import ceu.biolab.cmm.compoundDetails.service.CompoundDetailsService;
import ceu.biolab.cmm.config.GlobalExceptionHandler;
import ceu.biolab.cmm.legacyApi.controller.LegacyDetailsController;
import ceu.biolab.cmm.legacyApi.mapper.LegacyDetailsMapper;
import ceu.biolab.cmm.msSearch.service.CompoundService;
import ceu.biolab.cmm.shared.domain.compound.CMMCompound;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.OffsetDateTime;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LegacyDetailsController.class)
@Import({CompoundDetailsService.class, LegacyDetailsMapper.class, GlobalExceptionHandler.class})
class LegacyDetailsContractTest {
    @Autowired MockMvc mvc;
    @MockBean CompoundService compounds;
    @MockBean CompoundDetailsRepository repository;
    private static final OffsetDateTime DATE = OffsetDateTime.parse("2020-01-02T03:04:05Z");

    @BeforeEach void fixtures() {
        var compound = CMMCompound.builder().compoundId(42).compoundName("Water").formula("H2O").mass(18)
                .chargeType(1).chargeNumber(1).hmdbID("HMDB42").pcID(123).build();
        when(compounds.getCompoundById(42)).thenReturn(compound);
        when(repository.findCompoundMetadata(42)).thenReturn(Optional.of(new CompoundMetadata(DATE,null)));
        when(repository.compoundClassyfire(42)).thenReturn(List.of(new EntityLink("CHEMONTID:0000152","Class",null)));
        when(repository.compoundReferences(42)).thenReturn(List.of(new EntityLink("1","Paper",null)));
        when(repository.findClassyfire("CHEMONTID:0000152")).thenReturn(Optional.of(new ClassyfireNodeResponse(
                "CHEMONTID:0000152","Class","CHEMONTID:0000000",null,null,null,null,null,null,null,null,null,
                "CHEMONTID:0000000",DATE,null)));
        when(repository.findOntology(1)).thenReturn(Optional.of(new OntologyTermResponse(1L,"Term",null,null,null,null,null,null,null,DATE,null)));
        when(repository.findReference(1)).thenReturn(Optional.of(new ReferenceResponse(1L,"Paper",null,null,DATE,null,List.of(),List.of())));
        when(repository.referenceCompounds(1)).thenReturn(List.of(new EntityLink("42","Water",null)));
        when(repository.findOrganism(1)).thenReturn(Optional.of(new OrganismResponse(1L,"Organism",null,1,DATE,null,List.of(),List.of(),List.of())));
        when(repository.organismHierarchy(1)).thenReturn(List.of(new HierarchyEntry("1","Organism",1)));
        when(repository.organismReferences(1)).thenReturn(List.of(new EntityLink("1","Paper","https://example.test/paper")));
    }

    @ParameterizedTest @ValueSource(strings={"/compounds/42","/classyfire/0000152","/ontology/1","/references/1","/organisms/1"})
    void everyLegacyDetailRouteReturnsJson(String path) throws Exception {
        mvc.perform(get("/api/v3"+path)).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.created").value("2020-01-02"));
    }
    @Test void classyfireUsesLegacyNamesAndShortIds() throws Exception {
        mvc.perform(get("/api/v3/classyfire/chemontid:0000152")).andExpect(status().isOk()).andExpect(content().json("""
                {"node_name":"Class","node_id":"0000152","kingdom":"0000000","super_class":null,"main_class":null,
                "sub_class":null,"direct_parent":"0000000","level_7":null,"level_8":null,"level_9":null,"level_10":null,
                "level_11":null,"created":"2020-01-02","last_updated":null}
                """,true));
    }
    @Test void ontologyKeepsNullDatabaseNumbersInsteadOfZero() throws Exception {
        mvc.perform(get("/api/v3/ontology/1")).andExpect(status().isOk()).andExpect(content().json("""
                {"ontology_term_id":1,"term":"Term","definition":null,"external_id":null,"external_source":null,
                "ontology_comment":null,"curator":null,"parent_id":null,"ontology_level":null,"created":"2020-01-02","last_updated":null}
                """,true));
    }
    @Test void referenceHasLegacyLinkedEntitiesAndNoModernPropertyNames() throws Exception {
        mvc.perform(get("/api/v3/references/1")).andExpect(status().isOk()).andExpect(content().json("""
                {"reference_id":1,"reference_text":"Paper","doi":null,"link":null,"compounds":[{"id":"42","name":"Water"}],
                "organisms":[],"created":"2020-01-02","last_updated":null}
                """,true));
    }
    @Test void organismHasNumberedHierarchyAndReferenceLinks() throws Exception {
        mvc.perform(get("/api/v3/organisms/1")).andExpect(status().isOk()).andExpect(content().json("""
                {"organism_id":1,"organism_name":"Organism","parent_id":null,"organism_level":1,
                "organism_hierarchy":[{"id":"1","name":"Organism","number":1}],"compounds":[],
                "references":[{"id":"https://example.test/paper","name":"Paper"}],"created":"2020-01-02","last_updated":null}
                """,true));
    }
    @Test void compoundUsesEnrichedCurrentDataWithLegacyFieldNames() throws Exception {
        mvc.perform(get("/api/v3/compounds/42")).andExpect(status().isOk())
                .andExpect(jsonPath("$.identifier").value(42)).andExpect(jsonPath("$.name").value("Water"))
                .andExpect(jsonPath("$.hmdb_id").value("HMDB42")).andExpect(jsonPath("$.pc_id").value(123))
                .andExpect(jsonPath("$.charge_type").value("positive")).andExpect(jsonPath("$.charge_number").value(1))
                .andExpect(jsonPath("$.classyfire_classification[0].id").value("0000152"))
                .andExpect(jsonPath("$.references[0].id").value("/api/v3/references/1"))
                .andExpect(jsonPath("$.compoundId").doesNotExist()).andExpect(jsonPath("$.pathways").doesNotExist());
        verify(compounds).getCompoundById(42);
    }
    @ParameterizedTest @ValueSource(strings={"/compounds/0","/compounds/not-an-id","/ontology/0","/references/-1","/organisms/abc","/classyfire/invalid"})
    void invalidIdsAre400BeforeDatabaseAccess(String path) throws Exception {
        mvc.perform(get("/api/v3"+path)).andExpect(status().isBadRequest());
        verifyNoInteractions(repository,compounds);
    }
    @ParameterizedTest @ValueSource(strings={"/compounds/999","/ontology/999","/references/999","/organisms/999","/classyfire/0000999"})
    void missingRecordsAre404(String path) throws Exception {
        mvc.perform(get("/api/v3"+path)).andExpect(status().isNotFound());
    }
    @Test void legacyRoutesDoNotOfferHtml() throws Exception {
        mvc.perform(get("/api/v3/references/1").accept(MediaType.TEXT_HTML)).andExpect(status().isNotAcceptable());
        verifyNoInteractions(repository,compounds);
    }
}
