package appeng.metrics.endpoint;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import appeng.metrics.Metrics;
import appeng.metrics.reporter.PrometheusReporter;

/**
 * Starts an embedded HTTP-Server that will expose the Metrics in a format
 * compatible with Prometheus.
 */
public class PrometheusEndpoint implements AutoCloseable {

    private final HttpServer server;

    private final PrometheusReporter reporter = new PrometheusReporter();

    public PrometheusEndpoint(String hostname, int port) {
        try {
            this.server = HttpServer.create();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create metrics HTTP-server", e);
        }

        InetSocketAddress address;
        if (hostname.isEmpty()) {
            address = new InetSocketAddress((InetAddress) null, port);
        } else {
            address = new InetSocketAddress(hostname, port);
        }

        // Default endpoint is /metrics in Prometheus
        this.server.createContext("/metrics", this::handler);

        try {
            this.server.bind(address, 0);
        } catch (IOException e) {
            throw new RuntimeException("Failed to bind metrics HTTP server to " + address, e);
        }

        this.server.start();

    }

    @Override
    public void close() {
        this.server.stop(0);
    }

    private synchronized void handler(HttpExchange exchange) throws IOException {
        // Update metrics
        reporter.reset();
        Metrics.visit(reporter);

        CharSequence report = getReport();

        exchange.getResponseHeaders().add("Content-Type", "text/plain; version=0.0.4");
        exchange.sendResponseHeaders(200, report.length());

        try (Writer writer = new OutputStreamWriter(exchange.getResponseBody(), StandardCharsets.US_ASCII)) {
            writer.append(report);
        }
        exchange.close();
    }

    private CharSequence getReport() {
        return reporter.getReport();
    }

}
