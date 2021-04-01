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

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.StringTextComponent;

/**
 * A modified version of the Minecraft text field. You can initialize it over the full element span. The mouse click
 * area is increased to the full element subtracted with the defined padding.
 *
 * The rendering does pay attention to the size of the '_' caret.
 */
public class AETextField extends TextFieldWidget {
    private static final int PADDING = 2;

    private final int _fontPad;
    private int selectionColor = 0xFF00FF00;

    /**
     * Uses the values to instantiate a padded version of a text field. Pays attention to the '_' caret.
     *
     * @param fontRenderer renderer for the strings
     * @param xPos         absolute left position
     * @param yPos         absolute top position
     * @param width        absolute width
     * @param height       absolute height
     */
    public AETextField(final FontRenderer fontRenderer, final int xPos, final int yPos, final int width,
            final int height) {
        super(fontRenderer, xPos + PADDING, yPos + PADDING,
                width - 2 * PADDING - fontRenderer.getStringWidth("_"), height - 2 * PADDING,
                StringTextComponent.EMPTY);

        this._fontPad = fontRenderer.getStringWidth("_");
    }

    public void selectAll() {
        this.setCursorPosition(0);
        this.setSelectionPos(this.getMaxStringLength());
    }

    public void setSelectionColor(int color) {
        this.selectionColor = color;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partial) {
        if (this.getVisible()) {
            if (this.isFocused()) {
                fill(matrixStack, this.x - PADDING + 1, this.y - PADDING + 1,
                        this.x + this.width + this._fontPad + PADDING - 1, this.y + this.height + PADDING - 1,
                        0xFF606060);
            } else {
                fill(matrixStack, this.x - PADDING + 1, this.y - PADDING + 1,
                        this.x + this.width + this._fontPad + PADDING - 1, this.y + this.height + PADDING - 1,
                        0xFFA8A8A8);
            }
            super.renderButton(matrixStack, mouseX, mouseY, partial);
        }
    }

    @Override
    public void drawSelectionBox(int startX, int startY, int endX, int endY) {
        if (!this.isFocused()) {
            return;
        }

        if (startX < endX) {
            int i = startX;
            startX = endX;
            endX = i;
        }

        startX += 1;
        endX -= 1;

        if (startY < endY) {
            int j = startY;
            startY = endY;
            endY = j;
        }

        startY -= PADDING;

        if (endX > this.x + this.width) {
            endX = this.x + this.width;
        }

        if (startX > this.x + this.width) {
            startX = this.x + this.width;
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        float red = (this.selectionColor >> 16 & 255) / 255.0F;
        float blue = (this.selectionColor >> 8 & 255) / 255.0F;
        float green = (this.selectionColor & 255) / 255.0F;
        float alpha = (this.selectionColor >> 24 & 255) / 255.0F;

        RenderSystem.color4f(red, green, blue, alpha);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(startX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, startY, 0.0D).endVertex();
        bufferbuilder.pos(startX, startY, 0.0D).endVertex();
        tessellator.draw();
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
        RenderSystem.color4f(1, 1, 1, 1);
    }

}
