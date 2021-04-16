package appeng.client.gui.style;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.util.JSONUtils;

public class Palette {

    private final EnumMap<PaletteColor, Color> palette;

    public Palette(EnumMap<PaletteColor, Color> palette) {
        this.palette = palette;
    }

    public Color get(PaletteColor color) {
        return palette.get(color);
    }

    public Palette merge(Palette otherPalette) {
        EnumMap<PaletteColor, Color> mergedPalette = new EnumMap<>(palette);
        mergedPalette.putAll(otherPalette.palette);
        return new Palette(mergedPalette);
    }

    public static class Deserializer implements JsonDeserializer<Palette> {
        @Override
        public Palette deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (!json.isJsonObject()) {
                throw new JsonParseException("Expected palette to be a JSON object.");
            }
            JsonObject paletteObj = json.getAsJsonObject();

            EnumMap<PaletteColor, Color> palette = new EnumMap<>(PaletteColor.class);

            for (Map.Entry<String, JsonElement> entry : paletteObj.entrySet()) {
                PaletteColor colorKey;
                try {
                    colorKey = PaletteColor.valueOf(entry.getKey());
                } catch (IllegalArgumentException e) {
                    throw new JsonParseException("Invalid palette color: " + entry.getKey() + " valid values: "
                            + Arrays.asList(PaletteColor.values()));
                }

                if (!JSONUtils.isString(entry.getValue())) {
                    throw new JsonParseException("Color palette entry for " + entry.getKey() + " is not a string");
                }

                palette.put(colorKey, Color.parse(entry.getValue().getAsString()));
            }

            // Check for missing entries
            for (PaletteColor value : PaletteColor.values()) {
                if (!palette.containsKey(value)) {
                    throw new JsonParseException("Missing color in palette: " + value);
                }
            }

            return new Palette(palette);
        }
    }

}
