package ceu.biolab.cmm.unit.browseSearch;

import ceu.biolab.cmm.shared.repository.CompoundPathways;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.compound.Pathway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import java.sql.ResultSet;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BrowseSearchRepositoryTest {
    @Test
    void bulkPathwaysCachesMissingRowsAndCopiesMutableValues() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        ResultSet rs = mock(ResultSet.class);
        when(rs.getInt("compound_id")).thenReturn(123);
        when(rs.getInt("pathway_id")).thenReturn(1);
        when(rs.getString("pathway_name")).thenReturn("Glycolysis");
        when(rs.getString("pathway_map")).thenReturn("map00010");
        doAnswer(call -> { ((RowCallbackHandler) call.getArgument(1)).processRow(rs); return null; })
                .when(jdbc).query(any(PreparedStatementCreator.class), any(RowCallbackHandler.class));
        Compound first = Compound.builder().compoundId(123).build();
        Compound empty = Compound.builder().compoundId(456).build();
        Map<Integer, Set<Pathway>> cache = new HashMap<>();
        CompoundPathways.hydrate(jdbc, List.of(first, empty), cache);
        assertEquals(Set.of(new Pathway(1, "Glycolysis", "map00010")), first.getPathways());
        assertTrue(empty.getPathways().isEmpty());
        first.getPathways().iterator().next().setPathwayName("changed");
        Compound repeated = Compound.builder().compoundId(123).build();
        CompoundPathways.hydrate(jdbc, List.of(repeated, empty), cache);
        assertEquals("Glycolysis", repeated.getPathways().iterator().next().getPathwayName());
        verify(jdbc, times(1)).query(any(PreparedStatementCreator.class), any(RowCallbackHandler.class));
    }
}
