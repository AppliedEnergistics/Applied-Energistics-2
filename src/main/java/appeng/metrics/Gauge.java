package appeng.metrics;

import java.util.function.Supplier;

/**
 * A Gauge is a Metric that will simply read the current value of something when
 * the metrics are being reported.
 */
class Gauge extends Metric {

    private final Supplier<Number> valueSupplier;

    public Gauge(String id, Supplier<Number> valueSupplier) {
        super(id);
        this.valueSupplier = valueSupplier;
    }

    public Supplier<Number> getValueSupplier() {
        return valueSupplier;
    }

    public Number getValue() {
        return valueSupplier.get();
    }

    @Override
    public void accept(MetricVisitor visitor) {
        visitor.visitGauge(getId(), getValue());
    }

}
