package ceu.biolab.cmm.metadata.service;

import java.time.Duration;
import java.util.Objects;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/** One lazily refreshed value; synchronized refresh prevents concurrent database reloads. */
final class TimedCache<T> {
    private final LongSupplier clock;
    private T value;
    private long loadedAt;

    TimedCache() { this(System::nanoTime); }
    TimedCache(LongSupplier clock) { this.clock = clock; }

    synchronized T get(Duration ttl, Supplier<T> loader) {
        if (ttl.isNegative()) throw new IllegalArgumentException("Metadata cache TTL must not be negative");
        if (ttl.isZero()) return loader.get();
        if (value == null || clock.getAsLong() - loadedAt >= ttl.toNanos()) {
            T refreshed = Objects.requireNonNull(loader.get());
            value = refreshed;
            loadedAt = clock.getAsLong();
        }
        return value;
    }
}
