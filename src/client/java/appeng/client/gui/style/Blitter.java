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

package appeng.client.gui.style;

import java.util.Objects;
import java.util.function.Function;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import org.joml.Matrix4f;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.TriState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.core.AppEng;

/**
 * Utility class for drawing rectangular textures in the UI.
 */
@OnlyIn(Dist.CLIENT)
public final class Blitter {
    public static final RenderPipeline GUI_TEXTURED_OPAQUE = RenderPipelines.GUI_TEXTURED.toBuilder()
            .withLocation(AppEng.makeId("pipeline/gui_textured_opaque"))
            .withoutBlend()
            .build();

    public static final Function<ResourceLocation, RenderType> GUI_TEXTURED_OPAQUE_TYPE = Util.memoize(
            textureId -> RenderType.create(
                    "ae2:gui_textured_opaque",
                    1536,
                    GUI_TEXTURED_OPAQUE,
                    RenderType.CompositeState.builder()
                            .setTextureState(new RenderStateShard.TextureStateShard(textureId, TriState.DEFAULT, false))
                            .createCompositeState(false)));

    // This assumption is obviously bogus, but currently all textures are this size,
    // and it's impossible to get the texture size from an already loaded texture.
    // The coordinates will still be correct when a resource pack provides bigger textures as long
    // as each texture element is still positioned at the same relative position
    public static final int DEFAULT_TEXTURE_WIDTH = 256;
    public static final int DEFAULT_TEXTURE_HEIGHT = 256;

    private final ResourceLocation texture;
    // This texture size is only used to convert the source rectangle into uv coordinates (which are [0,1] and work
    // with textures of any size at runtime).
    private final int referenceWidth;
    private final int referenceHeight;
    private int r = 255;
    private int g = 255;
    private int b = 255;
    private int a = 255;
    private Rect2i srcRect;
    private Rect2i destRect = new Rect2i(0, 0, 0, 0);
    private boolean blending = true;
    private TextureTransform transform = TextureTransform.NONE;
    private int zOffset;

    Blitter(ResourceLocation texture, int referenceWidth, int referenceHeight) {
        this.texture = texture;
        this.referenceWidth = referenceWidth;
        this.referenceHeight = referenceHeight;
    }

    /**
     * Creates a blitter where the source rectangle is in relation to a 256x256 pixel texture.
     */
    public static Blitter texture(ResourceLocation file) {
        return texture(file, DEFAULT_TEXTURE_WIDTH, DEFAULT_TEXTURE_HEIGHT);
    }

    /**
     * Creates a blitter where the source rectangle is in relation to a 256x256 pixel texture.
     */
    public static Blitter texture(String file) {
        return texture(file, DEFAULT_TEXTURE_WIDTH, DEFAULT_TEXTURE_HEIGHT);
    }

    /**
     * Creates a blitter where the source rectangle is in relation to a texture of the given size.
     */
    public static Blitter texture(ResourceLocation file, int referenceWidth, int referenceHeight) {
        return new Blitter(file, referenceWidth, referenceHeight);
    }

    /**
     * Creates a blitter where the source rectangle is in relation to a texture of the given size.
     */
    public static Blitter texture(String file, int referenceWidth, int referenceHeight) {
        return new Blitter(AppEng.makeId("textures/" + file), referenceWidth, referenceHeight);
    }

    /**
     * Creates a blitter from a texture atlas sprite.
     */
    public static Blitter sprite(TextureAtlasSprite sprite) {
        var atlas = (TextureAtlas) Minecraft.getInstance().getTextureManager().getTexture(sprite.atlasLocation());

        return new Blitter(sprite.atlasLocation(), atlas.getWidth(), atlas.getHeight())
                .src(
                        sprite.getX(),
                        sprite.getY(),
                        sprite.contents().width(),
                        sprite.contents().height());
    }

    public static Blitter guiSprite(ResourceLocation resourceLocation) {
        var sprites = Minecraft.getInstance().getGuiSprites();
        var sprite = sprites.getSprite(resourceLocation);
        return sprite(sprite);
    }

    public Blitter copy() {
        Blitter result = new Blitter(texture, referenceWidth, referenceHeight);
        result.srcRect = srcRect;
        result.destRect = destRect;
        result.r = r;
        result.g = g;
        result.b = b;
        result.a = a;
        return result;
    }

    public int getSrcX() {
        return srcRect == null ? 0 : srcRect.getX();
    }

    public int getSrcY() {
        return srcRect == null ? 0 : srcRect.getY();
    }

    public int getSrcWidth() {
        return srcRect == null ? destRect.getWidth() : srcRect.getWidth();
    }

    public int getSrcHeight() {
        return srcRect == null ? destRect.getHeight() : srcRect.getHeight();
    }

    /**
     * Use the given rectangle from the texture (in pixels assuming a 256x256 texture size).
     */
    public Blitter src(int x, int y, int w, int h) {
        this.srcRect = new Rect2i(x, y, w, h);
        return this;
    }

    public Blitter srcWidth(int w) {
        this.srcRect = new Rect2i(srcRect.getX(), srcRect.getY(), w, srcRect.getHeight());
        return this;
    }

    public Blitter srcHeight(int h) {
        this.srcRect = new Rect2i(srcRect.getX(), srcRect.getY(), srcRect.getWidth(), h);
        return this;
    }

    /**
     * Use the given rectangle from the texture (in pixels assuming a 256x256 texture size).
     */
    public Blitter src(Rect2i rect) {
        return src(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    /**
     * Draw into the rectangle defined by the given coordinates.
     */
    public Blitter dest(int x, int y, int w, int h) {
        this.destRect = new Rect2i(x, y, w, h);
        return this;
    }

    /**
     * Draw at the given x,y coordinate and use the source rectangle size as the destination rectangle size.
     */
    public Blitter dest(int x, int y) {
        return dest(x, y, 0, 0);
    }

    /**
     * Draw into the given rectangle.
     */
    public Blitter dest(Rect2i rect) {
        return dest(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    public Rect2i getDestRect() {
        int x = destRect.getX();
        int y = destRect.getY();
        int w = 0, h = 0;
        if (destRect.getWidth() != 0 && destRect.getHeight() != 0) {
            w = destRect.getWidth();
            h = destRect.getHeight();
        } else if (srcRect != null) {
            w = srcRect.getWidth();
            h = srcRect.getHeight();
        }
        return new Rect2i(x, y, w, h);
    }

    public Blitter color(float r, float g, float b) {
        this.r = (int) (Mth.clamp(r, 0, 1) * 255);
        this.g = (int) (Mth.clamp(g, 0, 1) * 255);
        this.b = (int) (Mth.clamp(b, 0, 1) * 255);
        return this;
    }

    public Blitter colorArgb(int packedArgb) {
        this.a = ARGB.alpha(packedArgb);
        this.r = ARGB.red(packedArgb);
        this.g = ARGB.green(packedArgb);
        this.b = ARGB.blue(packedArgb);
        return this;
    }

    public Blitter opacity(float a) {
        this.a = (int) (Mth.clamp(a, 0, 1) * 255);
        return this;
    }

    public Blitter color(float r, float g, float b, float a) {
        return color(r, g, b).opacity(a);
    }

    public Blitter transform(TextureTransform transform) {
        this.transform = Objects.requireNonNull(transform);
        return this;
    }

    /**
     * Enables or disables alpha-blending. If disabled, all pixels of the texture will be drawn as opaque, and the alpha
     * value set using {@link #opacity(float)} will be ignored.
     */
    public Blitter blending(boolean enable) {
        this.blending = enable;
        return this;
    }

    /**
     * Sets the color to the R,G,B values encoded in the lower 24-bit of the given integer.
     */
    public Blitter colorRgb(int packedRgb) {
        float r = (packedRgb >> 16 & 255) / 255.0F;
        float g = (packedRgb >> 8 & 255) / 255.0F;
        float b = (packedRgb & 255) / 255.0F;

        return color(r, g, b);
    }

    public Blitter zOffset(int offset) {
        this.zOffset = offset;
        return this;
    }

    public void blit(GuiGraphics guiGraphics) {
        // With no source rectangle, we'll use the entirety of the texture. This happens rarely though.
        float minU, minV, maxU, maxV;
        if (srcRect == null) {
            minU = minV = 0;
            maxU = maxV = 1;
        } else {
            minU = srcRect.getX() / (float) referenceWidth;
            minV = srcRect.getY() / (float) referenceHeight;
            maxU = (srcRect.getX() + srcRect.getWidth()) / (float) referenceWidth;
            maxV = (srcRect.getY() + srcRect.getHeight()) / (float) referenceHeight;
        }

        // Transform the UV
        switch (transform) {
            case MIRROR_H -> {
                var tmp = minU;
                minU = maxU;
                maxU = tmp;
            }
            case MIRROR_V -> {
                var tmp = minV;
                minV = maxV;
                maxV = tmp;
            }
        }

        // It's possible to not set a destination rectangle size, in which case the
        // source rectangle size will be used
        float x1 = destRect.getX();
        float y1 = destRect.getY();
        float x2 = x1, y2 = y1;
        if (destRect.getWidth() != 0 && destRect.getHeight() != 0) {
            x2 += destRect.getWidth();
            y2 += destRect.getHeight();
        } else if (srcRect != null) {
            x2 += srcRect.getWidth();
            y2 += srcRect.getHeight();
        }

        Matrix4f matrix = guiGraphics.pose().last().pose();

        var bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.addVertex(matrix, x1, y2, zOffset)
                .setUv(minU, maxV)
                .setColor(r, g, b, a);
        bufferbuilder.addVertex(matrix, x2, y2, zOffset)
                .setUv(maxU, maxV)
                .setColor(r, g, b, a);
        bufferbuilder.addVertex(matrix, x2, y1, zOffset)
                .setUv(maxU, minV)
                .setColor(r, g, b, a);
        bufferbuilder.addVertex(matrix, x1, y1, zOffset)
                .setUv(minU, minV)
                .setColor(r, g, b, a);

        if (blending) {
            RenderType.guiTextured(texture).draw(bufferbuilder.buildOrThrow());
        } else {
            GUI_TEXTURED_OPAQUE_TYPE.apply(texture).draw(bufferbuilder.buildOrThrow());
        }
    }

}
