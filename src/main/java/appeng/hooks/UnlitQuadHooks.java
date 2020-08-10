package appeng.hooks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import appeng.core.AppEng;
import appeng.mixins.unlitquad.BakedQuadAccessor;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.render.model.json.ModelElementTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Direction;

/**
 * Implementation details of allowing quads to be defined as "unlit" in JSON
 * models by specifying an "unlit" boolean property on the face.
 */
public class UnlitQuadHooks {

    /**
     * Vertex format used for blocks.
     */
    private static final VertexFormat VERTEX_FORMAT = VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL;

    /**
     * Offset into the vertex data (which is represented as integers).
     */
    private static final int LIGHT_OFFSET = getLightOffset();

    // Lightmap texture coordinate with full intensity light leading to no drop in
    // brightness
    private static final int UNLIT_LIGHT_UV = LightmapTextureManager.pack(15, 15);

    /**
     * Thread-Local flag to indicate that an enhanced Applied Energistics model is
     * currently being deserialized.
     */
    private static final ThreadLocal<Boolean> ENABLE_UNLIT_EXTENSIONS = new ThreadLocal<>();

    /**
     * Notify the unlit model system that a specific model is about to be
     * deserialized by {@link net.minecraft.client.render.model.ModelLoader}.
     */
    public static void beginDeserializingModel(Identifier location) {
        String namespace = location.getNamespace();
        if (namespace.equals(AppEng.MOD_ID)) {
            ENABLE_UNLIT_EXTENSIONS.set(true);
        }
    }

    /**
     * Notify the unlit model system that deserialization of a model has ended.
     */
    public static void endDeserializingModel() {
        ENABLE_UNLIT_EXTENSIONS.set(false);
    }

    public static boolean isUnlitExtensionEnabled() {
        Boolean b = ENABLE_UNLIT_EXTENSIONS.get();
        return b != null && b;
    }

    public static ModelElementFace enhanceModelElementFace(ModelElementFace modelElement, JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (JsonHelper.getBoolean(jsonObject, "unlit", false)) {
            return new UnlitModelElementFace(modelElement.cullFace, modelElement.tintIndex, modelElement.textureId,
                    modelElement.textureData);
        }
        return modelElement;
    }

    /**
     * Creates a new quad from the given quad and pre-bakes it to not be affected by
     * lighting (neither diffuse lighting nor the prebaked lightmap). This works on
     * the assumption that Vanilla will not modify a quad's lightmap data if it's
     * not zero.
     */
    public static BakedQuad makeUnlit(BakedQuad quad) {
        int[] vertexData = quad.getVertexData().clone();
        int stride = VERTEX_FORMAT.getVertexSizeInteger();
        // Set the pre-baked texture coords for the lightmap.
        // Vanilla will not overwrite them if they are non-zero
        for (int i = 0; i < 4; i++) {
            vertexData[stride * i + LIGHT_OFFSET] = UNLIT_LIGHT_UV;
        }
        Sprite sprite = ((BakedQuadAccessor) quad).getSprite();
        // Copy the quad to disable diffuse lighting
        return new BakedQuad(vertexData, quad.getColorIndex(), quad.getFace(), sprite, false /* diffuse lighting */);
    }

    /**
     * This subclass is used as a marker to indicate this face deserialized from
     * JSON is supposed to be unlit, which translates to processing by
     * {@link #makeUnlit(BakedQuad)}.
     */
    public static class UnlitModelElementFace extends ModelElementFace {
        public UnlitModelElementFace(Direction cullFaceIn, int tintIndexIn, String textureIn, ModelElementTexture blockFaceUVIn) {
            super(cullFaceIn, tintIndexIn, textureIn, blockFaceUVIn);
        }
    }

    /**
     * Find the index in the BakedQuad vertex data (for vertex 0) where the lightmap
     * coordinates are. Assumes the BLOCK vertex format.
     */
    private static int getLightOffset() {
        int offset = 0;
        for (VertexFormatElement element : VERTEX_FORMAT.getElements()) {
            // LIGHT_ELEMENT is the lightmap vertex element
            if (element == VertexFormats.LIGHT_ELEMENT) {
                if (element.getFormat() != VertexFormatElement.Format.SHORT) {
                    throw new UnsupportedOperationException("Expected light map format to be of type SHORT");
                }
                if (offset % 4 != 0) {
                    throw new UnsupportedOperationException("Expected light map offset to be 4-byte aligned");
                }
                return offset / 4;
            }
            offset += element.getSize();
        }
        throw new UnsupportedOperationException("Failed to find the lightmap index in the block vertex format");
    }

}
