package appeng.metrics;

import java.util.Objects;

abstract class Metric {

    private final String id;

    public Metric(String id) {
        this.id = Objects.requireNonNull(id);
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Metric metric = (Metric) o;
        return id.equals(metric.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public abstract void accept(MetricVisitor visitor);

}
