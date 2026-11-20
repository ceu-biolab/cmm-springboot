package ceu.biolab.cmm.unit.shared.domain.compound;

import ceu.biolab.cmm.msSearch.domain.compound.CompoundMapper;
import org.junit.jupiter.api.Test;
import java.sql.ResultSet;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NullableChargeTest {
    @Test
    void sqlNullChargeRemainsDistinctFromKnownZero() throws Exception {
        var rs = mock(ResultSet.class);
        var unknown = CompoundMapper.toCompound(CompoundMapper.fromResultSet(rs));
        assertNull(unknown.getChargeType());
        assertNull(unknown.getChargeNumber());
        when(rs.getObject("charge_type")).thenReturn(0);
        when(rs.getObject("charge_number")).thenReturn(0);
        var neutral = CompoundMapper.toCompound(CompoundMapper.fromResultSet(rs));
        assertEquals(0, neutral.getChargeType());
        assertEquals(0, neutral.getChargeNumber());
    }
}
