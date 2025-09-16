package ceu.biolab.cmm.unit.MSMSSearch.dto;

import ceu.biolab.cmm.MSMSSearch.dto.MSMSSearchRequestDTO;
import ceu.biolab.cmm.MSMSSearch.domain.ScoreType;
import ceu.biolab.cmm.shared.domain.IonizationMode;
import ceu.biolab.cmm.shared.domain.MzToleranceMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MSMSSearchRequestDTOTest {
    @Test
    void defaults_areInitialized() {
        MSMSSearchRequestDTO dto = new MSMSSearchRequestDTO();
        assertNotNull(dto.getAdducts());
        assertNotNull(dto.getFragmentsMZsIntensities());
        assertEquals(0.0, dto.getPrecursorIonMZ());
        assertEquals(MzToleranceMode.MDA, dto.getToleranceModePrecursorIon());
        assertEquals(MzToleranceMode.MDA, dto.getToleranceModeFragments());
        assertEquals(IonizationMode.POSITIVE, dto.getIonizationMode());
        assertEquals(ScoreType.COSINE, dto.getScoreType());
    }
}

