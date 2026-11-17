package ceu.biolab.cmm.metadata.service;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;

class TimedCacheTest {
    @Test
    void expiresOnMonotonicClockAndRetriesFailedRefresh() {
        AtomicLong time = new AtomicLong();
        TimedCache<String> cache = new TimedCache<>(time::get);
        Duration ttl = Duration.ofNanos(10);
        assertEquals("first", cache.get(ttl, () -> "first"));
        time.set(9);
        assertEquals("first", cache.get(ttl, () -> "unexpected"));
        time.set(10);
        assertThrows(IllegalStateException.class, () -> cache.get(ttl, () -> { throw new IllegalStateException(); }));
        assertEquals("second", cache.get(ttl, () -> "second"));
        assertEquals("second", cache.get(ttl, () -> "unexpected"));
    }

    @Test
    void zeroDisablesCachingAndNegativeTtlIsInvalid() {
        TimedCache<Integer> cache = new TimedCache<>();
        AtomicInteger calls = new AtomicInteger();
        assertEquals(1, cache.get(Duration.ZERO, calls::incrementAndGet));
        assertEquals(2, cache.get(Duration.ZERO, calls::incrementAndGet));
        assertThrows(IllegalArgumentException.class, () -> cache.get(Duration.ofSeconds(-1), () -> 0));
    }

    @Test
    void concurrentRequestsLoadOnlyOnce() throws Exception {
        TimedCache<Integer> cache = new TimedCache<>();
        AtomicInteger loads = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(8)) {
            var results = new ArrayList<Future<Integer>>();
            for (int i = 0; i < 8; i++) results.add(executor.submit(() -> {
                start.await();
                return cache.get(Duration.ofMinutes(5), loads::incrementAndGet);
            }));
            start.countDown();
            for (var result : results) assertEquals(1, result.get(5, TimeUnit.SECONDS));
        }
        assertEquals(1, loads.get());
    }
}
