package ceu.biolab.cmm.integration.msSearch;

import ceu.biolab.cmm.msSearch.repository.CompoundRepository;
import ceu.biolab.cmm.shared.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SearchCorrectionsIntegrationTest {
    @Autowired private CompoundRepository repository;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ceu.biolab.cmm.browseSearch.service.BrowseSearchService browse;

    @Test
    void browseTreatsWildcardCharactersLiterally() {
        var request = new ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest();
        request.setDatabases(Set.of(Database.ALL));
        request.setFormula("%");
        assertTrue(browse.search(request).getCompoundlist().isEmpty());
        request.setFormula("");
        for (String name : java.util.List.of("%%%", "___", "\\%_")) {
            request.setCompoundName(name);
            assertTrue(browse.search(request).getCompoundlist().isEmpty());
        }
    }

    @Test
    @Transactional
    void classificationFieldsComeFromOneCompleteDatabaseRow() {
        int id = jdbc.queryForObject("SELECT compound_id FROM compounds WHERE compound_id > 0 ORDER BY compound_id LIMIT 1", Integer.class);
        // Transaction-local shadow tables exercise the real repository query
        // without changing the curated database or other tests' connections.
        jdbc.execute("CREATE TEMP TABLE compounds_lm_classification (compound_id int, lm_classification_id int) ON COMMIT DROP");
        jdbc.execute("CREATE TEMP TABLE lm_classification (lm_classification_id int, category text, main_class text, subclass text, class_level4 text) ON COMMIT DROP");
        jdbc.update("INSERT INTO compounds_lm_classification VALUES (?, 1), (?, 2)", id, id);
        jdbc.execute("INSERT INTO lm_classification VALUES (1, 'FA', 'FA01', 'FA0101', 'FA010101'), (2, 'GP', 'GP01', 'GP0102', NULL)");

        var compound = repository.findCompoundById(id).orElseThrow();

        assertEquals(1, compound.getLipidMapsClassifications().size());
        var classification = compound.getLipidMapsClassifications().iterator().next();
        assertEquals("FA", classification.getCategory());
        assertEquals("FA01", classification.getMainClass());
        assertEquals("FA0101", classification.getSubClass());
        assertEquals("FA010101", classification.getClassLevel4());
    }

    @Test
    void detectedAdductLeavesAlternativeGroupsEmpty() {
        var result = repository.annotateMSFeature(400.3432, MzToleranceMode.PPM, 50d,
                IonizationMode.POSITIVE, Optional.of("[M+H]+"), Optional.empty(),
                Set.of("[M+H]+", "[M+Na]+"), Set.of(Database.HMDB), MetaboliteType.ALL);
        var groups = result.getFirst().getAnnotationsByAdducts();
        assertEquals(2, groups.size());
        assertTrue(groups.stream().filter(g -> g.getAdduct().equals("[M+Na]+"))
                .allMatch(g -> g.getAnnotations().isEmpty()));
        assertFalse(groups.stream().filter(g -> g.getAdduct().equals("[M+H]+"))
                .findFirst().orElseThrow().getAnnotations().isEmpty());
    }
}
