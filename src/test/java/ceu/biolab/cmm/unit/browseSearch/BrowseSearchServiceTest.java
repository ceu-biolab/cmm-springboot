package ceu.biolab.cmm.unit.browseSearch;

import ceu.biolab.cmm.browseSearch.dto.BrowseQueryResponse;
import ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest;
import ceu.biolab.cmm.browseSearch.repository.BrowseSearchRepository;
import ceu.biolab.cmm.browseSearch.service.BrowseSearchService;
import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.compound.CompoundType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrowseSearchServiceTest {

    @Mock
    private BrowseSearchRepository repository;

    private BrowseSearchService service;

    @BeforeEach
    void setUp() {
        service = new BrowseSearchService(repository);
    }

    @Test
    void searchWithFormulaDelegatesToRepository() throws Exception {
        BrowseSearchRequest request = new BrowseSearchRequest();
        request.setFormula("C6H12O6");
        request.setDatabases(Set.of(Database.ALL));
        request.setMetaboliteType(MetaboliteType.ALL);

        Compound compound = Compound.builder()
                .compoundId(1)
                .compoundName("Glucose")
                .formula("C6H12O6")
                .mass(180.0)
                .chargeType(0)
                .chargeNumber(0)
                .formulaType(FormulaType.CHNOPS)
                .compoundType(CompoundType.NON_LIPID)
                .build();

        when(repository.findMatchingCompounds(any(BrowseSearchRequest.class)))
                .thenReturn(new BrowseQueryResponse(List.of(compound)));

        BrowseQueryResponse response = service.search(request);

        assertFalse(response.getCompoundlist().isEmpty());
        verify(repository).findMatchingCompounds(request);
    }

    @Test
    void searchThrowsWhenNameAndFormulaMissing() {
        BrowseSearchRequest request = new BrowseSearchRequest();

        assertThrows(ResponseStatusException.class, () -> service.search(request));
        verifyNoInteractions(repository);
    }

    @Test
    void searchThrowsWhenDatabasesMissing() {
        BrowseSearchRequest request = new BrowseSearchRequest();
        request.setFormula("C6H12O6");
        request.setDatabases(Set.of());
        request.setMetaboliteType(MetaboliteType.ALL);

        assertThrows(ResponseStatusException.class, () -> service.search(request));
        verifyNoInteractions(repository);
    }

    @Test
    void searchThrowsWhenMetaboliteMissing() {
        BrowseSearchRequest request = new BrowseSearchRequest();
        request.setFormula("C6H12O6");
        request.setDatabases(Set.of(Database.ALL));
        request.setMetaboliteType(null);

        assertThrows(ResponseStatusException.class, () -> service.search(request));
        verifyNoInteractions(repository);
    }

    @Test
    void constructorKeepsProvidedFormulaAndName() {
        BrowseSearchRequest request = new BrowseSearchRequest(
                "Glucose",
                "C6H12O6",
                Set.of(Database.ALL),
                MetaboliteType.ALL,
                false
        );

        assertEquals("Glucose", request.getCompoundName());
        assertEquals("C6H12O6", request.getFormula());
    }
}
