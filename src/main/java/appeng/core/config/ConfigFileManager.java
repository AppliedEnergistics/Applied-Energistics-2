package appeng.core.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class ConfigFileManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().create();

    private final ConfigSection rootSection;

    private final File file;

    private boolean loading;

    public ConfigFileManager(ConfigSection rootSection, File file) {
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
        try (InputStream in = new FileInputStream(file)) {
            JsonObject rootObj = GSON.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), JsonObject.class);
            rootSection.read(rootObj);
        } catch (FileNotFoundException ignored) {
        } catch (Exception e) {
            throw new RuntimeException("Failed to load AE2 config: " + file, e);
        } finally {
            loading = false;
        }
    }

    public void save() {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            try {
                Files.createDirectories(parent.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Failed to create AE2 config directory: " + parent);
            }
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(rootSection.write(), writer);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write AE2 config: " + file, e);
        }
        rootSection.write();
    }

}
