package ceu.biolab.cmm.unit.compoundDetails;

import ceu.biolab.cmm.compoundDetails.repository.CompoundDetailsRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompoundDetailsRepositoryTest {
    @Test void ontologyNullNumbersStayNullAndIdIsBound() throws Exception {
        var jdbc = mock(JdbcTemplate.class);
        var rs = mock(ResultSet.class);
        when(rs.getObject("ontology_term_id")).thenReturn(12L);
        when(rs.getObject("parent_id")).thenReturn(null);
        when(rs.getObject("ontology_level")).thenReturn(null);
        when(jdbc.query(anyString(), any(RowMapper.class), eq(12L))).thenAnswer(call ->
                List.of(((RowMapper<?>)call.getArgument(1)).mapRow(rs,0)));
        var value = new CompoundDetailsRepository(jdbc).findOntology(12).orElseThrow();
        assertEquals(12L,value.ontologyTermId()); assertNull(value.parentId()); assertNull(value.ontologyLevel());
        var sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc).query(sql.capture(),any(RowMapper.class),eq(12L));
        assertTrue(sql.getValue().contains("ontology_term_id = ?"));
    }
    @Test void classyfireParentComesFromDeepestAncestorOtherThanSelf() throws Exception {
        var jdbc = mock(JdbcTemplate.class);
        var rs = mock(ResultSet.class);
        when(rs.getString("node_id")).thenReturn("CHEMONTID:0000009");
        when(rs.getString("kingdom")).thenReturn("CHEMONTID:0000000");
        when(rs.getString("level_5")).thenReturn("CHEMONTID:0000008");
        when(rs.getString("level_6")).thenReturn("CHEMONTID:0000009");
        when(jdbc.query(anyString(),any(RowMapper.class),eq("CHEMONTID:0000009"))).thenAnswer(call ->
                List.of(((RowMapper<?>)call.getArgument(1)).mapRow(rs,0)));
        var value = new CompoundDetailsRepository(jdbc).findClassyfire("CHEMONTID:0000009").orElseThrow();
        assertEquals("CHEMONTID:0000008",value.directParent());
        assertEquals("CHEMONTID:0000009",value.level6());
    }
    @Test void cyclicOrganismDataFailsInsteadOfLoopingOrReturningAnIncompleteTree() throws Exception {
        var jdbc = mock(JdbcTemplate.class);
        var rs = mock(ResultSet.class); when(rs.getBoolean("cycle")).thenReturn(true);
        when(jdbc.query(anyString(),any(RowMapper.class),eq(5L))).thenAnswer(call ->
                List.of(((RowMapper<?>)call.getArgument(1)).mapRow(rs,0)));
        assertThrows(IllegalStateException.class, () -> new CompoundDetailsRepository(jdbc).organismHierarchy(5));
    }
}
