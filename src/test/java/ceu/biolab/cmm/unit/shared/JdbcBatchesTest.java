package ceu.biolab.cmm.unit.shared;

import ceu.biolab.cmm.shared.repository.JdbcBatches;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;

class JdbcBatchesTest {
    @Test
    void partitionsWithoutDroppingReorderingOrLimitingValues() {
        List<Integer> values = IntStream.range(0, 10001).boxed().toList();
        var batches = JdbcBatches.partition(values);
        assertTrue(batches.size() > 1);
        assertEquals(values, batches.stream().flatMap(List::stream).toList());
        assertTrue(JdbcBatches.partition(List.of()).isEmpty());
    }
}
