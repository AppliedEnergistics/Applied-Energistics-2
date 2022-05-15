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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import appeng.client.Point;
import appeng.client.gui.style.Blitter;

/**
 * A modified version of the Minecraft text field. You can initialize it over the full element span. The mouse click
 * area is increased to the full element subtracted with the defined padding.
 * <p>
 * The rendering does pay attention to the size of the '_' caret.
 */
public class AETextField extends EditBox implements IResizableWidget, ITooltip {
    private static final Blitter BLITTER = Blitter.texture("guis/text_field.png", 128, 128);

    private static final int PADDING = 2;

    private final int _fontPad;
    private int selectionColor = 0xFF00FF00;
    private List<Component> tooltipMessage = Collections.emptyList();

    /**
     * Uses the values to instantiate a padded version of a text field. Pays attention to the '_' caret.
     *
     * @param fontRenderer renderer for the strings
     * @param xPos         absolute left position
     * @param yPos         absolute top position
     * @param width        absolute width
     * @param height       absolute height
     */
    public AETextField(Font fontRenderer, int xPos, int yPos, int width,
            int height) {
        super(fontRenderer, xPos + PADDING, yPos + PADDING,
                width - 2 * PADDING - fontRenderer.width("_"), height - 2 * PADDING,
                Component.empty());

        this._fontPad = fontRenderer.width("_");
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        // Swallow all key presses except for focus escape when we're focused to prevent "e" from
        // closing the window instead of typing into the text field
        return isFocused() && canConsumeInput() && keyCode != GLFW.GLFW_KEY_TAB && keyCode != GLFW.GLFW_KEY_ESCAPE;
    }

    @Override
    public void move(Point pos) {
        super.setX(pos.getX() + PADDING);
        this.y = pos.getY() + PADDING;
    }

    @Override
    public void resize(int width, int height) {
        super.setWidth(width - 2 * PADDING - _fontPad);
        this.height = height - 2 * PADDING;
    }

    public void selectAll() {
        this.moveCursorTo(0);
        this.setHighlightPos(this.getMaxLength());
    }

    public void setSelectionColor(int color) {
        this.selectionColor = color;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partial) {
        if (this.isVisible()) {
            var yOffset = 0;
            if (!this.isEditable()) {
                yOffset = 12;
            } else if (isFocused()) {
                yOffset = 24;
            }

            // Render background
            int left = x - PADDING;
            int top = y - PADDING;
            int right = left + width + 2 * PADDING + _fontPad;

            BLITTER.src(0, yOffset, 1, 12)
                    .dest(left, top)
                    .blit(poseStack, getBlitOffset());
            var backgroundWidth = Math.min(126, right - left - 2);
            BLITTER.src(1, yOffset, backgroundWidth, 12)
                    .dest(left + 1, top)
                    .blit(poseStack, getBlitOffset());
            BLITTER.src(127, yOffset, 1, 12)
                    .dest(right - 1, top)
                    .blit(poseStack, getBlitOffset());

            super.renderButton(poseStack, mouseX, mouseY, partial);
        }
    }

    @Override
    public void renderHighlight(int startX, int startY, int endX, int endY) {
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

        endX = Mth.clamp(endX, this.x, this.x + this.width);
        startX = Mth.clamp(startX, this.x, this.x + this.width);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();

        float red = (this.selectionColor >> 16 & 255) / 255.0F;
        float blue = (this.selectionColor >> 8 & 255) / 255.0F;
        float green = (this.selectionColor & 255) / 255.0F;
        float alpha = (this.selectionColor >> 24 & 255) / 255.0F;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(startX, endY, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferbuilder.vertex(endX, endY, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferbuilder.vertex(endX, startY, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferbuilder.vertex(startX, startY, 0.0D).color(red, green, blue, alpha).endVertex();
        tessellator.end();
        RenderSystem.disableColorLogicOp();
    }

    @Override
    public Rect2i getTooltipArea() {
        return new Rect2i(
                x - PADDING,
                y - PADDING,
                width + 2 * PADDING + _fontPad,
                height + 2 * PADDING);
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return visible;
    }

    @NotNull
    @Override
    public List<Component> getTooltipMessage() {
        return tooltipMessage;
    }

    public void setTooltipMessage(List<Component> tooltipMessage) {
        this.tooltipMessage = Objects.requireNonNull(tooltipMessage);
    }
}
