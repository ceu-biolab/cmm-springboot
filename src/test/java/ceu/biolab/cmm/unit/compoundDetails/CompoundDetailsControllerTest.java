package ceu.biolab.cmm.unit.compoundDetails;

import ceu.biolab.cmm.compoundDetails.controller.ReferenceDataController;
import ceu.biolab.cmm.compoundDetails.dto.*;
import ceu.biolab.cmm.compoundDetails.repository.CompoundDetailsRepository;
import ceu.biolab.cmm.compoundDetails.service.CompoundDetailsService;
import ceu.biolab.cmm.config.GlobalExceptionHandler;
import ceu.biolab.cmm.msSearch.controller.CompoundController;
import ceu.biolab.cmm.msSearch.service.CompoundService;
import ceu.biolab.cmm.shared.domain.compound.CMMCompound;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({ReferenceDataController.class, CompoundController.class})
@Import({CompoundDetailsService.class, GlobalExceptionHandler.class})
class CompoundDetailsControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @MockBean CompoundService compounds;
    @MockBean CompoundDetailsRepository repository;

    @Test void existingCompoundPropertiesStayFlatAndRelationshipsAreAdditive() throws Exception {
        var compound = CMMCompound.builder().compoundId(42).compoundName("Water").formula("H2O").mass(18).hmdbID("HMDB42").build();
        when(compounds.getCompoundById(42)).thenReturn(compound);
        when(repository.findCompoundMetadata(42)).thenReturn(Optional.of(new CompoundMetadata(null,null)));
        when(repository.compoundReferences(42)).thenReturn(List.of(new EntityLink("1","Paper",null)));
        var result = mvc.perform(get("/api/compounds/42")).andExpect(status().isOk())
                .andExpect(jsonPath("$.compoundId").value(42)).andExpect(jsonPath("$.hmdbID").value("HMDB42"))
                .andExpect(jsonPath("$.references[0].id").value("1"))
                .andExpect(jsonPath("$.compound").doesNotExist()).andReturn();
        var original = json.valueToTree(compound);
        var enriched = json.readTree(result.getResponse().getContentAsString());
        original.fields().forEachRemaining(field -> assertEquals(field.getValue(),enriched.get(field.getKey()),field.getKey()));
    }
    @ParameterizedTest @ValueSource(strings={"/ontology/0","/references/-1","/organisms/0","/ontology/abc","/references/99999999999999999999","/classyfire/invalid"})
    void invalidIdsAlwaysReturn400(String path) throws Exception {
        mvc.perform(get("/api"+path)).andExpect(status().isBadRequest());verifyNoInteractions(repository);
    }
    @ParameterizedTest @ValueSource(strings={"/ontology/999","/references/999","/organisms/999","/classyfire/0000999"})
    void missingRecordsReturn404(String path) throws Exception {
        mvc.perform(get("/api"+path)).andExpect(status().isNotFound());
    }
}
