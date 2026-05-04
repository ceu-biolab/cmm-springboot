package ceu.biolab.cmm.unit.metadata;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ceu.biolab.cmm.metadata.controller.AdductController;
import ceu.biolab.cmm.metadata.dto.AdductCatalogResponse;
import org.junit.jupiter.api.Test;

class AdductControllerTest {

    @Test
    void listAdductsReturnsPositiveAndNegativeLists() {
        AdductController controller = new AdductController();

        AdductCatalogResponse response = controller.listAdducts();

        assertFalse(response.positive().isEmpty(), "Positive adduct list should not be empty");
        assertFalse(response.negative().isEmpty(), "Negative adduct list should not be empty");
        assertTrue(response.positive().contains("[M+H]+"), "Positive adducts should include [M+H]+");
        assertTrue(response.negative().contains("[M-H]-"), "Negative adducts should include [M-H]-");
        assertEquals("[M+H]+", response.positive().get(0), "Most common positive adduct should be first");
        assertEquals("[M-H]-", response.negative().get(0), "Most common negative adduct should be first");
        assertTrue(response.positive().indexOf("[M+2H]2+") < response.positive().indexOf("[M+H-H2O]+"));
        assertTrue(response.negative().indexOf("[M-H-H2O]-") < response.negative().indexOf("[M+FA-H]-"));
    }
}
