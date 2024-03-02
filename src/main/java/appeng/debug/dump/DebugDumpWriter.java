package appeng.debug.dump;

import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DebugDumpWriter implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DebugDumpWriter.class);

    private final Path finalOutput;
    private final Path objectsTempPath;
    private final JsonWriter objectsWriter;
    private final Path gridsTempPath;
    private final JsonWriter gridsWriter;

    public DebugDumpWriter(Path finalOutput) throws IOException {
        this.finalOutput = finalOutput;
        this.objectsTempPath = Files.createTempFile("ae2debug_objects", ".json.gz");
        this.gridsTempPath = Files.createTempFile("ae2debug_grids", ".json.gz");
        this.objectsWriter = new JsonWriter(Files.newBufferedWriter(objectsTempPath));
        this.gridsWriter = new JsonWriter(Files.newBufferedWriter(gridsTempPath));
    }

    @Override
    public void close() throws Exception {


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
