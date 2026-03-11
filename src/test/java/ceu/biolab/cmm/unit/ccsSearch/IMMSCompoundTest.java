package ceu.biolab.cmm.unit.ccsSearch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ceu.biolab.cmm.ccsSearch.domain.IMMSCompound;
import ceu.biolab.cmm.shared.domain.FormulaType;
import ceu.biolab.cmm.shared.domain.compound.CMMCompound;
import ceu.biolab.cmm.shared.domain.compound.CompoundType;
import java.util.Set;
import org.junit.jupiter.api.Test;

class IMMSCompoundTest {

    @Test
    void constructorPreservesDatabaseIdentifiers() {
        CMMCompound base = CMMCompound.builder()
                .compoundId(7)
                .casId("CAS")
                .compoundName("Example")
                .formula("C2H4")
                .mass(28.0313)
                .chargeType(1)
                .chargeNumber(1)
                .formulaType(FormulaType.CHNOPS)
                .compoundType(CompoundType.NON_LIPID)
                .lipidType(null)
                .numChains(null)
                .numCarbons(null)
                .doubleBonds(null)
                .mol2(null)
                .pathways(Set.of())
                .keggID("C00001")
                .lmID("LMFA0001")
                .hmdbID("HMDB00001")
                .agilentID("AG001")
                .pcID(100)
                .chebiID(200)
                .inHouseID("INH-1")
                .aspergillusID(300)
                .knapsackID("KNAP001")
                .npatlasID(400)
                .fahfaID(500)
                .ohPositionID(600)
                .aspergillusWebName("Asp")
                .build();

        IMMSCompound compound = new IMMSCompound(base, 123.4);

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
    }
}
