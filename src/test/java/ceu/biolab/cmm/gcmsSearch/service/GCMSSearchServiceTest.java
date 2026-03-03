package ceu.biolab.cmm.gcmsSearch.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class GCMSSearchServiceTest {

    @Test
    void validateConfigurationRejectsThresholdAboveOne() {
        GCMSSearchService service = new GCMSSearchService();
        ReflectionTestUtils.setField(service, "gcmsScoreThreshold", 1.1d);

        assertThrows(IllegalStateException.class, service::validateConfiguration);
    }

    @Test
    void validateConfigurationAcceptsThresholdInRange() {
        GCMSSearchService service = new GCMSSearchService();
        ReflectionTestUtils.setField(service, "gcmsScoreThreshold", 0.5d);

        assertDoesNotThrow(service::validateConfiguration);
    }
}
