package ceu.biolab.cmm.unit.CEMSSearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ceu.biolab.cmm.CEMSSearch.domain.CemsCompoundMapper;
import ceu.biolab.cmm.CEMSSearch.dto.CemsQueryResponseDTO;
import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.shared.domain.compound.CMMCompound;
import ceu.biolab.cmm.shared.domain.compound.CompoundType;
import org.junit.jupiter.api.Test;

class CemsCompoundMapperTest {

    @Test
    void toCompoundMapsDatabaseIdentifiers() {
        CemsQueryResponseDTO dto = new CemsQueryResponseDTO();
        dto.setCompoundId(42L);
        dto.setCompoundName("Example");
        dto.setFormula("C2H4");
        dto.setMass(28.0313);
        dto.setChargeType(1L);
        dto.setChargeNumber(1L);
        dto.setCompoundType(CompoundType.NON_LIPID.getDbValue());
        dto.setFormulaTypeInt(FormulaType.CHNOPS.getFormulaTypeIntValue());
        dto.setKeggId("C00001");
        dto.setLmId("LMFA0001");
        dto.setHmdbId("HMDB00001");
        dto.setAgilentId("AG001");
        dto.setPcId(100);
        dto.setChebiId(200);
        dto.setInHouseId("INH-1");
        dto.setAspergillusId(300);
        dto.setKnapsackId("KNAP001");
        dto.setNpatlasId(400);
        dto.setFahfaId(500);
        dto.setOhPosition(600);
        dto.setAspergillusWebName("Asp");

        CMMCompound compound = CemsCompoundMapper.toCompound(dto);

        assertEquals("C00001", compound.getKeggID());
        assertEquals("LMFA0001", compound.getLmID());
        assertEquals("HMDB00001", compound.getHmdbID());
        assertEquals("AG001", compound.getAgilentID());
        assertEquals(100, compound.getPcID());
        assertEquals(200, compound.getChebiID());
        assertEquals("INH-1", compound.getInHouseID());
        assertEquals(300, compound.getAspergillusID());
        assertEquals("KNAP001", compound.getKnapsackID());
        assertEquals(400, compound.getNpatlasID());
        assertEquals(500, compound.getFahfaID());
        assertEquals(600, compound.getOhPositionID());
        assertEquals("Asp", compound.getAspergillusWebName());
        assertNull(compound.getMol2());
    }

    @Test
    void toCompoundThrowsWhenMassMissing() {
        CemsQueryResponseDTO dto = new CemsQueryResponseDTO();
        dto.setCompoundId(42L);
        dto.setChargeType(1L);
        dto.setChargeNumber(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> CemsCompoundMapper.toCompound(dto));
        assertTrue(ex.getMessage().contains("mass"));
    }

    @Test
    void toCompoundThrowsWhenChargeTypeMissing() {
        CemsQueryResponseDTO dto = new CemsQueryResponseDTO();
        dto.setCompoundId(42L);
        dto.setMass(28.0313);
        dto.setChargeNumber(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> CemsCompoundMapper.toCompound(dto));
        assertTrue(ex.getMessage().contains("chargeType"));
    }
}
