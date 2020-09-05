package appeng.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;

/**
 * Registry to register Metrics.
 */
public final class Metrics {

    private static final List<Metric> metrics = new ArrayList<>();

    private Metrics() {
    }

    /**
     * Registers a Gauge, which is a simple metric with no state that will be
     * queried for its current value whenever metrics are being reported.
     */
    public static synchronized void gauge(String id, Supplier<Number> valueSupplier) {
        metrics.add(new Gauge(id, valueSupplier));
    }

    /**
     * Register several standard metrics for a Guava {@link Cache}.
     */
    public static synchronized void cache(String name, Cache<?, ?> cache) {
        metrics.add(new CacheMetrics(name, () -> cache));
    }

    /**
     * Register several standard metrics for a Guava {@link Cache} based on a
     * supplier to support an underlying changing cache.
     */
    public static synchronized void cache(String name, Supplier<Cache<?, ?>> cacheSupplier) {
        metrics.add(new CacheMetrics(name, cacheSupplier));
    }

    public static synchronized void visit(MetricVisitor visitor) {
        for (Metric metric : metrics) {
            metric.accept(visitor);
        }
    }

}
