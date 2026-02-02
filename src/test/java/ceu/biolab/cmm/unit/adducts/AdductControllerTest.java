package ceu.biolab.cmm.unit.adducts;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ceu.biolab.cmm.adducts.controller.AdductController;
import ceu.biolab.cmm.adducts.dto.AdductCatalogResponse;
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
    }
}
