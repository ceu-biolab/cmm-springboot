package ceu.biolab.cmm.unit.browseSearch;

import ceu.biolab.cmm.browseSearch.repository.BrowseSearchRepository;
import ceu.biolab.cmm.shared.domain.compound.Pathway;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BrowseSearchRepositoryTest {

    @Test
    @SuppressWarnings("unchecked")
    void fetchPathwaysForCompound_returnsPathwaysFromDatabase() throws Exception {
        NamedParameterJdbcTemplate namedTemplate = mock(NamedParameterJdbcTemplate.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(namedTemplate.getJdbcTemplate()).thenReturn(jdbcTemplate);

        Pathway glycolysis = new Pathway(1, "Glycolysis", "map00010");
        Pathway pentose = new Pathway(2, "Pentose phosphate pathway", "map00030");
        when(jdbcTemplate.query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class)))
                .thenReturn(List.of(glycolysis, pentose));

        BrowseSearchRepository repository = new BrowseSearchRepository(namedTemplate, resourceLoader);

        Method method = BrowseSearchRepository.class.getDeclaredMethod("fetchPathwaysForCompound", int.class);
        method.setAccessible(true);
        Set<Pathway> pathways = (Set<Pathway>) method.invoke(repository, 123);

        assertEquals(Set.of(glycolysis, pentose), pathways);
        verify(jdbcTemplate).query(anyString(), any(PreparedStatementSetter.class), any(RowMapper.class));
    }
}
