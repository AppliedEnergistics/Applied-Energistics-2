package appeng.metrics;

public interface MetricVisitor {

    void visitGauge(String id, Number value);

}
