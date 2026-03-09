package ceu.biolab.cmm.unit.MSMSSearch.dto;

import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchRequestDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MSMSSearchRequestDTOTest {
    @Test
    void defaults_areInitialized() {
        MSMSSearchRequestDTO dto = new MSMSSearchRequestDTO();
        assertNotNull(dto.getAdducts());
        assertNotNull(dto.getFragmentsMZsIntensities());
        assertNull(dto.getCIDEnergy());
        assertEquals(0.0, dto.getPrecursorIonMZ());
        assertNull(dto.getToleranceModePrecursorIon());
        assertNull(dto.getToleranceModeFragments());
        assertNull(dto.getIonizationMode());
        assertNull(dto.getScoreType());
    }
}
