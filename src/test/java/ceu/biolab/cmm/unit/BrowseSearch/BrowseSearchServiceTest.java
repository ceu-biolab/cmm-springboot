package ceu.biolab.cmm.unit.BrowseSearch;


import ceu.biolab.cmm.browseSearch.dto.BrowseQueryResponse;
import ceu.biolab.cmm.browseSearch.dto.BrowseSearchRequest;
import ceu.biolab.cmm.browseSearch.dto.BrowseSearchResponse;
import ceu.biolab.cmm.browseSearch.repository.BrowseSearchRepository;
import ceu.biolab.cmm.browseSearch.service.BrowseSearchService;
import ceu.biolab.cmm.shared.domain.Database;
import ceu.biolab.cmm.shared.domain.MetaboliteType;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BrowseSearchServiceTest {
    private BrowseSearchRepository browseSearchRepository;
    private BrowseSearchService browseSearchService;

    @BeforeEach
    public void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(); // no necesita conexión real si no se usa
      dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:testdb");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        // Instancia del template
        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        // Instancia del ResourceLoader
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        browseSearchRepository=new BrowseSearchRepository(jdbcTemplate,resourceLoader);
        // Creamos el servicio con las dependencias
        browseSearchService = new BrowseSearchService(browseSearchRepository);
    }

    @Test
    public void testSearchWithEmptyNameReturnsEmptyList() {
        // Arrang
        BrowseSearchRequest query = new BrowseSearchRequest ();
        query.setCompound_name("");
      // Simulamos string vacío
        query.setFormula("C6H12O6");
        List<Database> list=new ArrayList<>();
        list.add(Database.HMDB);
        query.setDatabases(list);
        query.setMetaboliteType(MetaboliteType.ALL);
        query.setExact_name(true);
       BrowseQueryResponse results = browseSearchService.search(query);
        System.out.printf(results.toString());
        assertFalse(results.getCompoundlist().isEmpty(), "Not expected empty list when ONLY name is empty");
    }
    @Test
    public void testSearchWithNullName() {
        // Arrange
        BrowseSearchRequest query = new BrowseSearchRequest ();
        query.setCompound_name(null);
        // Simulamos string vacío
        // Act
        BrowseQueryResponse results = browseSearchService.search(query);
        System.out.printf(results.toString());
        // Assert
        assertTrue(results.getCompoundlist().isEmpty(), "should not break when name is null" +
                "");
    }
    @Test
    public void testSearchWithCorrect() throws IOException {

        BrowseSearchRequest query = new BrowseSearchRequest ();
        query.setFormula("C6H12O6 ");
        query.setCompound_name("glucose");
        List<Database> databaseList = new ArrayList<>();
        databaseList.add(Database.ALL);
        query.setDatabases(databaseList );
        query.setMetaboliteType(MetaboliteType.ALL);
        query.setExact_name(true);
        // Simulamos string vacío
        // Act
        BrowseQueryResponse results = browseSearchService.search(query);
        // Assert
        assertTrue(results.getCompoundlist().isEmpty(), "should not break when name is null" + "");
    }


}
