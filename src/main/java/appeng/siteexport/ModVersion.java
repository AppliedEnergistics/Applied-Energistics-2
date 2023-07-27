package appeng.siteexport;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public final class ModVersion {

    private static boolean initialized;

    private static String modVersion;

    private ModVersion() {
    }

    public static String get() {
        if (initialized) {
            return modVersion;
        }
        initialized = true;

        // GHETTO!
        Process process = null;
        Path tempOutput = null;
        try {
            tempOutput = Files.createTempFile("gitoutput", ".txt");
            process = new ProcessBuilder("git", "rev-parse", "--short", "HEAD")
                    .redirectError(ProcessBuilder.Redirect.INHERIT).redirectInput(ProcessBuilder.Redirect.PIPE)
                    .redirectOutput(tempOutput.toFile()).start();
            if (process.waitFor(10, TimeUnit.SECONDS)) {
                String version = new String(Files.readAllBytes(tempOutput), StandardCharsets.UTF_8).trim();
                if (version.isEmpty()) {
                    throw new Exception("Empty git output");
                }
                modVersion = version;
                return version;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (tempOutput != null) {
                try {
                    Files.deleteIfExists(tempOutput);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (process != null) {
                try {
                    process.destroyForcibly();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}
