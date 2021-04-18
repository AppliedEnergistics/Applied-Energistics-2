package appeng.client.gui.style;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.JSONUtils;

/**
 * Deserializes a {@link Rectangle2d} either from an Array <code>[x,y,width,height]</code> or a JSON object with the
 * properties x, y, width, height (where x and y default to 0).
 */
public enum Rectangle2dDeserializer implements JsonDeserializer<Rectangle2d> {
    INSTANCE;

    @Override
    public Rectangle2d deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json.isJsonArray()) {
            JsonArray arr = json.getAsJsonArray();
            if (arr.size() != 4) {
                throw new JsonParseException("Rectangles expressed as arrays must have 4 elements.");
            }

            int x = arr.get(0).getAsInt();
            int y = arr.get(1).getAsInt();
            int width = arr.get(2).getAsInt();
            int height = arr.get(3).getAsInt();
            return new Rectangle2d(x, y, width, height);
        } else {
            JsonObject obj = json.getAsJsonObject();
            int x = JSONUtils.getInt(obj, "x", 0);
            int y = JSONUtils.getInt(obj, "y", 0);
            int width = JSONUtils.getInt(obj, "width");
            int height = JSONUtils.getInt(obj, "height");
            return new Rectangle2d(x, y, width, height);
        }
    }
}
