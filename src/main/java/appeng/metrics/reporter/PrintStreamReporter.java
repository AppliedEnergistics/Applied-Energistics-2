package appeng.metrics.reporter;

import java.io.PrintStream;

import appeng.metrics.MetricVisitor;

/**
 * Reports Metrics as simple key-value pairs to a {@link PrintStream} (i.e.
 * {@link System#out}).
 */
public class PrintStreamReporter implements MetricVisitor {

    private final PrintStream out;

    public PrintStreamReporter(PrintStream out) {
        this.out = out;
    }

    @Override
    public void visitGauge(String id, Number value) {
        out.println(id + '=' + value);
    }

}
