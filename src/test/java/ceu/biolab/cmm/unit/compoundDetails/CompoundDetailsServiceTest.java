package ceu.biolab.cmm.unit.compoundDetails;

import ceu.biolab.cmm.compoundDetails.dto.*;
import ceu.biolab.cmm.compoundDetails.repository.CompoundDetailsRepository;
import ceu.biolab.cmm.compoundDetails.service.CompoundDetailsService;
import ceu.biolab.cmm.msSearch.service.CompoundService;
import ceu.biolab.cmm.shared.domain.compound.CMMCompound;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompoundDetailsServiceTest {
    private final CompoundService compounds = mock(CompoundService.class);
    private final CompoundDetailsRepository repository = mock(CompoundDetailsRepository.class);
    private final CompoundDetailsService service = new CompoundDetailsService(compounds, repository);

    @Test void detailEnrichmentPreservesTheExistingCompound() {
        var compound = CMMCompound.builder().compoundId(42).compoundName("Water").mass(18).build();
        when(compounds.getCompoundById(42)).thenReturn(compound);
        when(repository.findCompoundMetadata(42)).thenReturn(Optional.of(new CompoundMetadata(null, null)));
        var reference = new EntityLink("1", "Paper", "https://example.test/paper");
        when(repository.compoundReferences(42)).thenReturn(List.of(reference));
        var result = service.compound(42);
        assertSame(compound, result.compound());
        assertEquals(List.of(reference), result.references());
        assertNull(result.created());
    }
    @ParameterizedTest @ValueSource(strings = {"0000152", "chemontid:0000152", "CHEMONTID:0000152"})
    void canonicalClassyfireIdsReachTheRepository(String input) {
        when(repository.findClassyfire("CHEMONTID:0000152")).thenReturn(Optional.empty());
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> service.classyfire(input)).getStatusCode().value());
        verify(repository).findClassyfire("CHEMONTID:0000152");
    }
    @ParameterizedTest @ValueSource(strings = {"bad", "1", "0000152' OR 1=1", "CHEMONTID:00001521", ""})
    void invalidClassyfireIdsAre400WithoutDatabaseAccess(String id) {
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> service.classyfire(id)).getStatusCode().value());
        verifyNoInteractions(repository);
    }
    @Test void missingAndInvalidNumericRecordsAreDistinguished() {
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> service.ontology(0)).getStatusCode().value());
        verifyNoInteractions(repository);
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> service.ontology(123)).getStatusCode().value());
    }
    @Test void referencesIncludeBothRelationshipCollections() {
        when(repository.findReference(1)).thenReturn(Optional.of(new ReferenceResponse(1L,"Paper",null,null,null,null,List.of(),List.of())));
        var compound = new EntityLink("42", "Compound", null);
        var organism = new EntityLink("5", "Species", null);
        when(repository.referenceCompounds(1)).thenReturn(List.of(compound));
        when(repository.referenceOrganisms(1)).thenReturn(List.of(organism));
        var result = service.reference(1);
        assertEquals(List.of(compound),result.compounds()); assertEquals(List.of(organism),result.organisms());
        assertNull(result.doi());
    }
}
