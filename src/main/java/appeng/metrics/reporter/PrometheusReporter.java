package appeng.metrics.reporter;

import appeng.metrics.MetricVisitor;

/**
 * Reports the metrics in a format compatible with Prometheus.
 * <p>
 * See <a href=
 * "https://prometheus.io/docs/instrumenting/exposition_formats/">Prometheus
 * Docs</a>
 */
public class PrometheusReporter implements MetricVisitor {

    private final StringBuilder response = new StringBuilder();

    @Override
    public void visitGauge(String id, Number value) {
        printType(id, "gauge");
        appendId(id);
        response.append(' ').append(value.toString()).append('\n');
    }

    private void printType(String id, String type) {
        response.append("# TYPE ");
        appendId(id);
        response.append(' ').append(type).append('\n');
    }

    public void reset() {
        response.setLength(0);
    }

    public int length() {
        return response.length();
    }

    public CharSequence getReport() {
        return response;
    }

    private void appendId(String id) {
        response.append("appeng_");
        // Sanitize to [a-zA-Z0-9:_]
        for (int i = 0; i < id.length(); i++) {
            char ch = id.charAt(i);
            if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9' || ch == ':' || ch == '_') {
                response.append(ch);
            } else {
                response.append('_');
            }
        }
    }

}
