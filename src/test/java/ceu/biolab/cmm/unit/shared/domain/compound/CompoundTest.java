package ceu.biolab.cmm.unit.shared.domain.compound;

import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.shared.domain.compound.Compound;
import ceu.biolab.cmm.shared.domain.compound.CompoundType;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CompoundTest {

    @Test
    void formulaElementsReturnsOrderedUniqueSymbols() {
        Compound compound = Compound.builder()
                .compoundId(1)
                .casId("")
                .compoundName("")
                .formula("C16H22Cl2O3")
                .mass(0.0)
                .chargeType(0)
                .chargeNumber(0)
                .formulaType(FormulaType.CHNOPSCL)
                .compoundType(CompoundType.LIPID)
                .build();

        Optional<Set<String>> elements = compound.formulaElements();

        assertTrue(elements.isPresent());
        assertEquals(4, elements.get().size());
        assertArrayEquals(new String[]{"C", "H", "Cl", "O"}, elements.get().toArray(new String[0]),
                "Elements should keep first-appearance order");
        assertEquals("CHClO", compound.formulaAlphabet().orElse(""));
    }

    @Test
    void formulaElementsEmptyWhenFormulaMissing() {
        Compound compound = new Compound();
        compound.setFormula(null);

        assertTrue(compound.formulaElements().isEmpty(), "Null formula should yield empty optional");
        assertTrue(compound.formulaAlphabet().isEmpty());
    }
}
