package appeng.metrics;

import java.util.function.Supplier;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;

/**
 * Cache Metrics simplify reporting of metrics for a Guava {@link Cache}.
 */
public class CacheMetrics extends Metric {

    private final Supplier<Cache<?, ?>> cacheSupplier;

    // Pre-allocate these to avoid repeated concatenation during reporting
    private final String sizeId;
    private final String hitCountId;
    private final String missCountId;
    private final String loadSuccessCountId;
    private final String loadExceptionCountId;
    private final String totalLoadTimeId;
    private final String evictionCountId;

    public CacheMetrics(String id, Supplier<Cache<?, ?>> cacheSupplier) {
        super(id);
        this.cacheSupplier = cacheSupplier;

        this.sizeId = id + ".size";
        this.hitCountId = id + ".hit-count";
        this.missCountId = id + ".miss-count";
        this.loadSuccessCountId = id + ".load-success-count";
        this.loadExceptionCountId = id + ".load-exception-count";
        this.totalLoadTimeId = id + ".total-load-time";
        this.evictionCountId = id + ".eviction-count";
    }

    @Override
    public void accept(MetricVisitor visitor) {
        Cache<?, ?> cache = this.cacheSupplier.get();

        if (cache != null) {
            visitor.visitGauge(sizeId, cache.size()); // Thread-safe

            CacheStats stats = cache.stats(); // Thread-safe
            visitor.visitGauge(hitCountId, stats.hitCount());
            visitor.visitGauge(missCountId, stats.missCount());
            visitor.visitGauge(loadSuccessCountId, stats.loadSuccessCount());
            visitor.visitGauge(loadExceptionCountId, stats.loadExceptionCount());
            visitor.visitGauge(totalLoadTimeId, stats.totalLoadTime());
            visitor.visitGauge(evictionCountId, stats.evictionCount());
        }
    }

}
