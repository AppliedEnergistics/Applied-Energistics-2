package appeng.client.gui.style;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

enum BlitterDeserializer implements JsonDeserializer<Blitter> {

    INSTANCE;

    @Override
    public Blitter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (!json.isJsonObject()) {
            throw new JsonParseException("Blitters must be objects");
        }

        JsonObject root = json.getAsJsonObject();

        String texture = JSONUtils.getString(root, "texture");
        int textureWidth = JSONUtils.getInt(root, "textureWidth", Blitter.DEFAULT_TEXTURE_WIDTH);
        int textureHeight = JSONUtils.getInt(root, "textureHeight", Blitter.DEFAULT_TEXTURE_HEIGHT);

        Blitter blitter;
        if (texture.contains(":")) {
            blitter = new Blitter(new ResourceLocation(texture), textureWidth, textureHeight);
        } else {
            blitter = Blitter.texture(texture, textureWidth, textureHeight);
        }

        if (root.has("srcRect")) {
            Rectangle2d srcRect = context.deserialize(root.get("srcRect"), Rectangle2d.class);
            blitter = blitter.src(srcRect);
        }

        return blitter;
    }

}
