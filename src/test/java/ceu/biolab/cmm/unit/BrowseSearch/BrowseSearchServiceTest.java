package ceu.biolab.cmm.unit.BrowseSearch;


import ceu.biolab.cmm.browseSearch.dto.BrowseQueryResponse;
import ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest;

import ceu.biolab.cmm.browseSearch.repository.BrowseSearchRepository;
import ceu.biolab.cmm.browseSearch.service.BrowseSearchService;
import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@SpringBootTest
@ActiveProfiles("local")
public class BrowseSearchServiceTest {
    private BrowseSearchRepository browseSearchRepository;
    private BrowseSearchService browseSearchService;
    @Autowired
    private DataSource dataSource;

    @BeforeEach

    public void setUp() {

        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);


        browseSearchRepository= new BrowseSearchRepository(jdbcTemplate, new DefaultResourceLoader());
        browseSearchService = new BrowseSearchService(browseSearchRepository);
    }


    @Test
    public void testSearchWithoutNameReturnsEmptyList() {

        BrowseSearchRequest query = new BrowseSearchRequest ();
        query.setFormula("C6H12O6");
        List<Database> list=new ArrayList<>();
        list.add(Database.ALL);
        query.setDatabases(list);
        query.setMetaboliteType(MetaboliteType.ALL);
        query.setExactName(false);
       BrowseQueryResponse results = browseSearchService.search(query);

        assertFalse(results.getCompoundlist().isEmpty(), "Not expected empty list when ONLY name is empty");
    }
    @Test
    public void testSearchWithoutFormula() {
        BrowseSearchRequest query = new BrowseSearchRequest ();
        query.setCompoundName(null);
        BrowseQueryResponse results = browseSearchService.search(query);

        assertTrue(results.getCompoundlist().isEmpty(), "should not break when name is null" +
                "");
    }
    @Test
    public void testSearchWithCorrect() throws IOException {

        BrowseSearchRequest query = new BrowseSearchRequest ();
        query.setFormula("C6H12O6 ");
        query.setCompoundName("glucose");
        List<Database> databaseList = new ArrayList<>();
        databaseList.add(Database.ALL);
        query.setDatabases(databaseList );
        query.setMetaboliteType(MetaboliteType.ALL);
        query.setExactName(true);

        BrowseQueryResponse results = browseSearchService.search(query);

        assertTrue(results.getCompoundlist().isEmpty(), "should not break when name is null" + "");
    }
    @Test
    public void testSearchWithoutDatabase() throws IOException {

        BrowseSearchRequest query = new BrowseSearchRequest ();
        query.setFormula("C6H12O6 ");
        query.setCompoundName("glucose");
        List<Database> databaseList = new ArrayList<>();
        query.setDatabases(databaseList );
        query.setMetaboliteType(MetaboliteType.ALL);
        query.setExactName(true);

        BrowseQueryResponse results = browseSearchService.search(query);

        assertTrue(results.getCompoundlist().isEmpty(), "should not break when name is null" + "");
    }

    @Test
    public void testSearchWithoutMetaboliteType() throws IOException {

        BrowseSearchRequest query = new BrowseSearchRequest ();
        query.setFormula("C6H12O6 ");
        query.setCompoundName("glucose");
        List<Database> databaseList = new ArrayList<>();
        databaseList.add(Database.ALL);
        query.setDatabases(databaseList );
        query.setMetaboliteType(null);
        query.setExactName(true);

        BrowseQueryResponse results = browseSearchService.search(query);

        assertTrue(results.getCompoundlist().isEmpty(), "should not break when name is null" + "");
    }


}
