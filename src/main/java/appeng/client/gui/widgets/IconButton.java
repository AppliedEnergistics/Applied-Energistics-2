/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public abstract class IconButton extends ButtonWidget implements ITooltip {
    public static final Identifier TEXTURE_STATES = new Identifier("appliedenergistics2",
            "textures/guis/states.png");

    private boolean halfSize = false;

    public IconButton(final int x, final int y, PressAction onPress) {
        super(x, y, 16, 16, LiteralText.EMPTY, onPress);
    }

    public void setVisibility(final boolean vis) {
        this.visible = vis;
        this.active = vis;
    }

    @Override
    public void renderButton(MatrixStack matrices, final int mouseX, final int mouseY, float partial) {

        MinecraftClient minecraft = MinecraftClient.getInstance();

        if (this.visible) {
            final int iconIndex = this.getIconIndex();

            TextureManager textureManager = minecraft.getTextureManager();
            textureManager.bindTexture(TEXTURE_STATES);
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend(); // FIXME: This should be the _default_ state, but some vanilla widget disables
                                        // it :|
            if (this.halfSize) {
                this.width = 8;
                this.height = 8;

                RenderSystem.pushMatrix();
                RenderSystem.translatef(this.x, this.y, 0.0F);
                RenderSystem.scalef(0.5f, 0.5f, 0.5f);

                if (this.active) {
                    RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                } else {
                    RenderSystem.color4f(0.5f, 0.5f, 0.5f, 1.0f);
                }

                final int uv_y = iconIndex / 16;
                final int uv_x = iconIndex - uv_y * 16;

                drawTexture(matrices, 0, 0, 256 - 16, 256 - 16, 16, 16);
                drawTexture(matrices, 0, 0, uv_x * 16, uv_y * 16, 16, 16);
                RenderSystem.popMatrix();
            } else {
                if (this.active) {
                    RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                } else {
                    RenderSystem.color4f(0.5f, 0.5f, 0.5f, 1.0f);
                }

                final int uv_y = iconIndex / 16;
                final int uv_x = iconIndex - uv_y * 16;

                drawTexture(matrices, this.x, this.y, 256 - 16, 256 - 16, 16, 16);
                drawTexture(matrices, this.x, this.y, uv_x * 16, uv_y * 16, 16, 16);
            }
            RenderSystem.enableDepthTest();
        }
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    protected abstract int getIconIndex();

    @Override
    public int xPos() {
        return this.x;
    }

    @Override
    public int yPos() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return this.halfSize ? 8 : 16;
    }

    @Override
    public int getHeight() {
        return this.halfSize ? 8 : 16;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    public boolean isHalfSize() {
        return this.halfSize;
    }

    public void setHalfSize(final boolean halfSize) {
        this.halfSize = halfSize;
    }

}
