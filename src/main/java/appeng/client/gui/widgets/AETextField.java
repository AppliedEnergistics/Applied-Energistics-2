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
import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import appeng.client.Point;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;

/**
 * A modified version of the Minecraft text field. You can initialize it over the full element span. The mouse click
 * area is increased to the full element subtracted with the defined padding.
 * <p>
 * The rendering does pay attention to the size of the '_' caret.
 */
public class AETextField extends EditBox implements IResizableWidget, ITooltip {
    private static final Blitter BLITTER = Blitter.texture("guis/text_field.png", 128, 128);

    private static final int PADDING = 2;

    private final int fontPad;
    private final ScreenStyle style;
    private int selectionColor;
    private List<Component> tooltipMessage = Collections.emptyList();

    /**
     * Displayed with a muted text color when the text box is unfocused and has no content.
     */
    @Nullable
    private Component placeholder;

    /**
     * Uses the values to instantiate a padded version of a text field. Pays attention to the '_' caret.
     *
     * @param fontRenderer renderer for the strings
     * @param xPos         absolute left position
     * @param yPos         absolute top position
     * @param width        absolute width
     * @param height       absolute height
     */
    public AETextField(ScreenStyle style, Font fontRenderer, int xPos, int yPos, int width,
            int height) {
        super(fontRenderer, xPos + PADDING, yPos + PADDING,
                width - 2 * PADDING - fontRenderer.width("_"), height - 2 * PADDING,
                Component.empty());

        this.style = style;
        this.fontPad = fontRenderer.width("_");
        setSelectionColor(style.getColor(PaletteColor.TEXTFIELD_SELECTION).toARGB());
        setTextColor(style.getColor(PaletteColor.TEXTFIELD_TEXT).toARGB());
    }

    // Extend the clickable area by the padding so we don't have a mouse deadzone that is still visually within
    // the background we render.
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        var bounds = getVisualBounds();
        return mouseX >= bounds.left && mouseX < bounds.right
                && mouseY >= bounds.top && mouseY < bounds.bottom;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // This hack is used to allow the standard mouse-click logic to recognize our clicks
        // that are on the padding, but not really inside the edit box.
        if (isMouseOver(mouseX, mouseY)) {
            mouseX = Mth.clamp(mouseX, getX(), getX() + width - 1);
            mouseY = Mth.clamp(mouseY, getY(), getY() + height - 1);
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
        setY(pos.getY() + PADDING);
    }

    @Override
    public void resize(int width, int height) {
        super.setWidth(width - 2 * PADDING - fontPad);
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
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partial) {
        if (this.isVisible()) {
            var yOffset = 0;
            if (!this.isEditable()) {
                yOffset = 12;
            } else if (isFocused()) {
                yOffset = 24;
            }

            var bounds = getVisualBounds();

            BLITTER.src(0, yOffset, 1, 12)
                    .dest(bounds.left, bounds.top)
                    .blit(poseStack);
            var backgroundWidth = Math.min(126, bounds.right - bounds.left - 2);
            BLITTER.src(1, yOffset, backgroundWidth, 12)
                    .dest(bounds.left + 1, bounds.top)
                    .blit(poseStack);
            BLITTER.src(127, yOffset, 1, 12)
                    .dest(bounds.right - 1, bounds.top)
                    .blit(poseStack);

            super.renderWidget(poseStack, mouseX, mouseY, partial);

            // Render a placeholder value if the text field isn't focused and is empty
            if (placeholder != null && !isFocused() && getValue().isEmpty()) {
                var font = Minecraft.getInstance().font;
                font.draw(poseStack, placeholder, getX(), getY(),
                        style.getColor(PaletteColor.TEXTFIELD_PLACEHOLDER).toARGB());
            }
        }
    }

    @Override
    public void renderHighlight(PoseStack pose, int startX, int startY, int endX, int endY) {
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

        endX = Mth.clamp(endX, getX(), getX() + this.width);
        startX = Mth.clamp(startX, getX(), getX() + this.width);

        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        fill(pose, startX, startY, endX, endY, this.selectionColor);
        RenderSystem.disableColorLogicOp();
    }

    @Override
    public Rect2i getTooltipArea() {
        return new Rect2i(
                getX() - PADDING,
                getY() - PADDING,
                width + 2 * PADDING + fontPad,
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

    private VisualBounds getVisualBounds() {
        // Render background
        int left = getX() - PADDING;
        int top = getY() - PADDING;
        int right = left + width + 2 * PADDING + fontPad;
        return new VisualBounds(
                left,
                top,
                right,
                top + height + 2 * PADDING);
    }

    public void setPlaceholder(Component placeholder) {
        this.placeholder = placeholder;
    }

    public Component getPlaceholder() {
        return placeholder;
    }

    private record VisualBounds(int left, int top, int right, int bottom) {
    }
}
