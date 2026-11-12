package ceu.biolab.cmm.unit.gcmsSearch.service;

import ceu.biolab.cmm.gcmsSearch.service.GCMSSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GCMSSearchServiceTest {

    @Test
    void validateConfigurationRejectsThresholdAboveOne() {
        GCMSSearchService service = new GCMSSearchService();
        ReflectionTestUtils.setField(service, "gcmsScoreThreshold", 1.1d);

        assertThrows(IllegalStateException.class, () ->
                ReflectionTestUtils.invokeMethod(service, "validateConfiguration"));
    }

    @Test
    void validateConfigurationAcceptsThresholdInRange() {
        GCMSSearchService service = new GCMSSearchService();
        ReflectionTestUtils.setField(service, "gcmsScoreThreshold", 0.5d);

        assertDoesNotThrow(() ->
                ReflectionTestUtils.invokeMethod(service, "validateConfiguration"));
    }
}
