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


import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.input.Keyboard;


/**
 * A modified version of the Minecraft text field.
 * You can initialize it over the full element span.
 * The mouse click area is increased to the full element
 * subtracted with the defined padding.
 * <p>
 * The rendering does pay attention to the size of the '_' caret.
 */
public class MEGuiTextField extends GuiTextField {
    private static final int PADDING = 2;

    private final int _xPos;
    private final int _yPos;
    private final int _width;
    private final int _height;
    private final int _fontPad;
    private int selectionColor = 0xFF00FF00;

    /**
     * Uses the values to instantiate a padded version of a text field.
     * Pays attention to the '_' caret.
     *
     * @param fontRenderer renderer for the strings
     * @param xPos         absolute left position
     * @param yPos         absolute top position
     * @param width        absolute width
     * @param height       absolute height
     */
    public MEGuiTextField(final FontRenderer fontRenderer, final int xPos, final int yPos, final int width, final int height) {
        super(0, fontRenderer, xPos + PADDING, yPos + PADDING, width - 2 * PADDING - fontRenderer.getCharWidth('_'), height - 2 * PADDING);

        this._fontPad = fontRenderer.getCharWidth('_');
        this._xPos = xPos;
        this._yPos = yPos;
        this._width = width;
        this._height = height;
    }

    public void onTextChange(final String oldText) {
    }

    @Override
    public boolean mouseClicked(final int xPos, final int yPos, final int button) {
        super.mouseClicked(xPos, yPos, button);

        final boolean requiresFocus = this.isMouseIn(xPos, yPos);
        if (!this.isFocused()) {
            this.setFocused(requiresFocus);
        }

        return true;
    }

    /**
     * Checks if the mouse is within the element
     *
     * @param xCoord current x coord of the mouse
     * @param yCoord current y coord of the mouse
     * @return true if mouse position is within the text field area
     */
    public boolean isMouseIn(final int xCoord, final int yCoord) {
        final boolean withinXRange = this._xPos <= xCoord && xCoord < this._xPos + this._width;
        final boolean withinYRange = this._yPos <= yCoord && yCoord < this._yPos + this._height;

        return withinXRange && withinYRange;
    }

    public boolean textboxKeyTyped(final char keyChar, final int keyID) {
        if (!isFocused()) {
            return false;
        }

        final String oldText = getText();
        boolean handled = super.textboxKeyTyped(keyChar, keyID);

        if (!handled
                && (keyID == Keyboard.KEY_RETURN
                || keyID == Keyboard.KEY_NUMPADENTER
                || keyID == Keyboard.KEY_ESCAPE)) {
            setFocused(false);
        }

        if (handled) {
            onTextChange(oldText);
        }

        return handled;
    }

    public void selectAll() {
        this.setCursorPosition(0);
        this.setSelectionPos(this.getMaxStringLength());
    }

    public void setSelectionColor(int color) {
        this.selectionColor = color;
    }

    @Override
    public void drawTextBox() {
        if (this.getVisible()) {
            if (this.isFocused()) {
                drawRect(this.x - PADDING + 1, this.y - PADDING + 1, this.x + this.width + this._fontPad + PADDING - 1, this.y + this.height + PADDING - 1,
                        0xFF606060);
            } else {
                drawRect(this.x - PADDING + 1, this.y - PADDING + 1, this.x + this.width + this._fontPad + PADDING - 1, this.y + this.height + PADDING - 1,
                        0xFFA8A8A8);
            }
            super.drawTextBox();
        }
    }

    public void setText(String text, boolean ignoreTrigger) {
        final String oldText = getText();

        super.setText(text);
        super.setCursorPositionEnd();

        if (!ignoreTrigger) {
            onTextChange(oldText);
        }
    }

    public void setText(String text) {
        setText(text, false);
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

        GlStateManager.color(red, green, blue, alpha);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(startX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, startY, 0.0D).endVertex();
        bufferbuilder.pos(startX, startY, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }

}
