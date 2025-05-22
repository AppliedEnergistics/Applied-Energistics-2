package appeng.core.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class ConfigFileManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().create();

    private final ConfigSection rootSection;

    private final Path file;

    private boolean loading;

    public ConfigFileManager(ConfigSection rootSection, Path file) {
        this.rootSection = rootSection;
        this.file = file;
        rootSection.setChangeListener(() -> {
            if (!loading) {
                save();
            }
        });
    }

    public void load() {
        loading = true;
        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonObject rootObj = GSON.fromJson(reader, JsonObject.class);
            rootSection.read(rootObj);
        } catch (FileNotFoundException ignored) {
        } catch (Exception e) {
            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException("Failed to load AE2 config: " + file, e);
        } finally {
            loading = false;
        }
    }

    public void save() {
        if (file.getParent() != null) {
            try {
                Files.createDirectories(file.getParent());
            } catch (IOException e) {
                throw new RuntimeException("Failed to create AE2 config directory: " + file.getParent());
            }
        }

        try (var writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            GSON.toJson(rootSection.write(), writer);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write AE2 config: " + file, e);
        }
        rootSection.write();
    }

}
