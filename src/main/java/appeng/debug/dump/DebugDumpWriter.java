package appeng.debug.dump;

import appeng.util.JsonStreamUtil;
import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;

public class DebugDumpWriter implements AutoCloseable {
    private static final String PROP_OBJECTS = "objects";
    private static final String PROP_GRIDS = "grids";

    private static final Logger LOG = LoggerFactory.getLogger(DebugDumpWriter.class);

    private final Map<String, Object> rootProperties = new HashMap<>();

    private final Path finalOutputPath;
    private final Path objectsTempPath;
    private final JsonWriter objectsWriter;
    private final Path gridsTempPath;
    private final JsonWriter gridsWriter;

    public DebugDumpWriter(Path outputPath) throws IOException {
        this.finalOutputPath = outputPath;
        this.objectsTempPath = Files.createTempFile("ae2debug_objects", ".json.gz");
        this.gridsTempPath = Files.createTempFile("ae2debug_grids", ".json.gz");
        this.objectsWriter = new JsonWriter(Files.newBufferedWriter(objectsTempPath));
        this.gridsWriter = new JsonWriter(Files.newBufferedWriter(gridsTempPath));
    }

    public void setProperty(String name, Object value) {
        if (PROP_GRIDS.equals(name) || PROP_OBJECTS.equals(name)) {
            throw new IllegalArgumentException("Property names " + PROP_GRIDS + " and " + PROP_OBJECTS + " are reserved");
        }

        rootProperties.put(name, value);
    }

    @Override
    public void close() throws Exception {
        gridsWriter.close();
        objectsWriter.close();

        // Recombine and write final output
        try (var out = new DeflaterOutputStream(Files.newOutputStream(finalOutputPath));
             var writer = new JsonWriter(new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)))) {

            writer.beginObject();
            JsonStreamUtil.writeProperties(rootProperties, writer);
            writer.name(PROP_GRIDS);
            writer.flush();
            try (var in = Files.newInputStream(gridsTempPath)) {
                in.transferTo(out);
            }
            writer.name(PROP_OBJECTS);
            writer.flush();
            try (var in = Files.newInputStream(objectsTempPath)) {
                in.transferTo(out);
            }
            writer.endObject();
        }

        try {
            Files.deleteIfExists(gridsTempPath);
        } catch (IOException e) {
            LOG.warn("Failed to delete temp file {}", gridsTempPath, e);
        }
        try {
            Files.deleteIfExists(objectsTempPath);
        } catch (IOException e) {
            LOG.warn("Failed to delete temp file {}", objectsTempPath, e);
        }
    }

}
