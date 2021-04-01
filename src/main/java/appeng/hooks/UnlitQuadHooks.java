/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.hooks;

import appeng.core.AppEng;
import appeng.mixins.unlitquad.BakedQuadAccessor;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.BlockFaceUV;
import net.minecraft.client.renderer.model.BlockPartFace;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

/**
 * Implementation details of allowing quads to be defined as "unlit" in JSON models by specifying an "unlit" boolean
 * property on the face.
 */
public class UnlitQuadHooks {

    /**
     * Offset into the vertex data (which is represented as integers).
     */
    private static final int LIGHT_OFFSET = getLightOffset();

    // Lightmap texture coordinate with full intensity light leading to no drop in
    // brightness
    private static final int UNLIT_LIGHT_UV = LightTexture.packLight(15, 15);

    /**
     * Thread-Local flag to indicate that an enhanced Applied Energistics model is currently being deserialized.
     */
    private static final ThreadLocal<Boolean> ENABLE_UNLIT_EXTENSIONS = new ThreadLocal<>();

    /**
     * Notify the unlit model system that a specific model is about to be deserialized by
     * {@link net.minecraft.client.renderer.model.ModelBakery}.
     */
    public static void beginDeserializingModel(ResourceLocation location) {
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

    public static BlockPartFace enhanceModelElementFace(BlockPartFace modelElement, JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (JSONUtils.getBoolean(jsonObject, "unlit", false)) {
            return new UnlitBlockPartFace(modelElement.cullFace, modelElement.tintIndex, modelElement.texture,
                    modelElement.blockFaceUV);
        }
        return modelElement;
    }

    /**
     * Creates a new quad from the given quad and pre-bakes it to not be affected by lighting (neither diffuse lighting
     * nor the prebaked lightmap). This works on the assumption that Vanilla will not modify a quad's lightmap data if
     * it's not zero.
     */
    public static BakedQuad makeUnlit(BakedQuad quad) {
        int[] vertexData = quad.getVertexData().clone();
        int stride = DefaultVertexFormats.BLOCK.getIntegerSize();
        // Set the pre-baked texture coords for the lightmap.
        // Vanilla will not overwrite them if they are non-zero
        for (int i = 0; i < 4; i++) {
            vertexData[stride * i + LIGHT_OFFSET] = UNLIT_LIGHT_UV;
        }
        TextureAtlasSprite sprite = ((BakedQuadAccessor) quad).getSprite();
        // Copy the quad to disable diffuse lighting
        return new BakedQuad(vertexData, quad.getTintIndex(), quad.getFace(), sprite, false /* diffuse lighting */);
    }

    /**
     * This subclass is used as a marker to indicate this face deserialized from JSON is supposed to be unlit, which
     * translates to processing by {@link #makeUnlit(BakedQuad)}.
     */
    public static class UnlitBlockPartFace extends BlockPartFace {
        public UnlitBlockPartFace(Direction cullFaceIn, int tintIndexIn, String textureIn, BlockFaceUV blockFaceUVIn) {
            super(cullFaceIn, tintIndexIn, textureIn, blockFaceUVIn);
        }
    }

    /**
     * Find the index in the BakedQuad vertex data (for vertex 0) where the lightmap coordinates are. Assumes the BLOCK
     * vertex format.
     */
    private static int getLightOffset() {
        VertexFormat format = DefaultVertexFormats.BLOCK;
        int offset = 0;
        for (VertexFormatElement element : format.getElements()) {
            // TEX_2SB is the lightmap vertex element
            if (element == DefaultVertexFormats.TEX_2SB) {
                if (element.getType() != VertexFormatElement.Type.SHORT) {
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
