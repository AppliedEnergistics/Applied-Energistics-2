/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2026, TeamAppliedEnergistics, All rights reserved.
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

import org.joml.Matrix3x2f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.TiledBlitRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;

/**
 * Draws sprites from the GUI sprite atlas using Vanilla GUI rendering. Since all sprites share the same atlas texture,
 * the GUI renderer batches them into very few draw calls.
 */
final class GuiGraphicsSpriteRenderer implements SpriteRenderer {
    private final GuiGraphicsExtractor guiGraphics;

    GuiGraphicsSpriteRenderer(GuiGraphicsExtractor guiGraphics) {
        this.guiGraphics = guiGraphics;
    }

    private static TextureAtlasSprite getAtlasSprite(Identifier sprite) {
        var atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI);
        return atlas.getSprite(Theme.resolveGuiSprite(sprite));
    }

    @Override
    public SpriteInfo getSprite(Identifier sprite) {
        var atlasSprite = getAtlasSprite(sprite);
        var scaling = atlasSprite.contents().getAdditionalMetadata(GuiMetadataSection.TYPE)
                .orElse(GuiMetadataSection.DEFAULT)
                .scaling();
        return SpriteInfo.of(atlasSprite.contents().width(), atlasSprite.contents().height(), scaling);
    }

    @Override
    public void blitRegion(Identifier sprite, int srcX, int srcY, int srcWidth, int srcHeight,
            int x, int y, int width, int height, boolean tile, int color) {
        if (width <= 0 || height <= 0 || srcWidth <= 0 || srcHeight <= 0) {
            return;
        }

        var atlasSprite = getAtlasSprite(sprite);
        var info = getSprite(sprite);
        var u0 = atlasSprite.getU(srcX / (float) info.width());
        var u1 = atlasSprite.getU((srcX + srcWidth) / (float) info.width());
        var v0 = atlasSprite.getV(srcY / (float) info.height());
        var v1 = atlasSprite.getV((srcY + srcHeight) / (float) info.height());

        var texture = Minecraft.getInstance().getTextureManager().getTexture(atlasSprite.atlasLocation());
        var textureSetup = TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler());
        var pose = new Matrix3x2f(guiGraphics.pose());

        if (tile && (width > srcWidth || height > srcHeight)) {
            guiGraphics.submitGuiElementRenderState(new TiledBlitRenderState(
                    RenderPipelines.GUI_TEXTURED,
                    textureSetup,
                    pose,
                    srcWidth, srcHeight,
                    x, y, x + width, y + height,
                    u0, u1, v0, v1,
                    color,
                    guiGraphics.peekScissorStack()));
        } else {
            if (tile) {
                // Crop the region instead of stretching it if the destination is smaller
                u1 = atlasSprite.getU((srcX + Math.min(width, srcWidth)) / (float) info.width());
                v1 = atlasSprite.getV((srcY + Math.min(height, srcHeight)) / (float) info.height());
            }
            guiGraphics.submitGuiElementRenderState(new BlitRenderState(
                    RenderPipelines.GUI_TEXTURED,
                    textureSetup,
                    pose,
                    x, y, x + width, y + height,
                    u0, u1, v0, v1,
                    color,
                    guiGraphics.peekScissorStack()));
        }
    }

    @Override
    public void blitSprite(Identifier sprite, int x, int y, int width, int height, int color) {
        if (width > 0 && height > 0) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, Theme.resolveGuiSprite(sprite), x, y, width, height,
                    color);
        }
    }
}
