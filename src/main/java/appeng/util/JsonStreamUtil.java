package appeng.util;

import java.io.IOException;
import java.util.Map;

import com.google.common.math.StatsAccumulator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;

import net.minecraft.world.level.ChunkPos;

public final class JsonStreamUtil {
    private static final Gson GSON = new GsonBuilder()
            .serializeSpecialFloatingPointValues()
            .create();

    private JsonStreamUtil() {
    }

    /**
     * Writes the entries of the given map as object properties. Assumes an object is currently open on the writer.
     */
    public static void writeProperties(Map<String, ?> properties, JsonWriter writer) throws IOException {
        for (var entry : properties.entrySet()) {
            writer.name(entry.getKey());
            GSON.toJson(entry.getValue(), entry.getValue().getClass(), writer);
        }
    }

    public static JsonElement toJson(ChunkPos pos) {
        var jsonPos = new JsonArray(2);
        jsonPos.add(pos.x);
        jsonPos.add(pos.z);
        return jsonPos;
    }

    public static Map<String, ?> toMap(StatsAccumulator stats) {
        if (stats.count() == 0) {
            return Map.of("count", 0);
        }

        return Map.of(
                "count", stats.count(),
                "min", stats.min(),
                "max", stats.max(),
                "mean", stats.mean());
    }
}
