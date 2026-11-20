package ceu.biolab.cmm.shared.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Internal SQL batches avoid driver parameter limits without limiting accepted requests. */
public final class JdbcBatches {
    private static final int IDS_PER_QUERY = 1000;
    private JdbcBatches() {}

    public static <T> List<List<T>> partition(Collection<T> values) {
        List<List<T>> batches = new ArrayList<>();
        List<T> batch = null;
        for (T value : values) {
            if (batch == null || batch.size() == IDS_PER_QUERY) {
                batch = new ArrayList<>();
                batches.add(batch);
            }
            batch.add(value);
        }
        return batches;
    }
}
