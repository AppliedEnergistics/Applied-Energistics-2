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

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

/**
 * Text that will be drawn on top of a {@link appeng.client.gui.AEBaseScreen}.
 */
public class Text {

    /**
     * The text to display.
     */
    private Component text = TextComponent.EMPTY;

    /**
     * The color to show the text in.
     */
    private PaletteColor color = PaletteColor.DEFAULT_TEXT_COLOR;

    /**
     * The position of the text on the screen.
     */
    private Position position;

    /**
     * Alignment relative to the computed x position.
     */
    private TextAlignment align = TextAlignment.LEFT;

    /**
     * Allows text to be scaled.
     */
    private float scale = 1.0f;

    /**
     * Allows text to be word-wrapped to fit a given maximum width.
     */
    private int maxWidth = 0;

    public Component getText() {
        return text;
    }

    public void setText(Component text) {
        this.text = text;
    }

    public PaletteColor getColor() {
        return color;
    }

    public void setColor(PaletteColor color) {
        this.color = color;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public TextAlignment getAlign() {
        return align;
    }

    public void setAlign(TextAlignment align) {
        this.align = align;
    }

    @Deprecated(forRemoval = true, since = "1.18")
    public void setCenterHorizontally(boolean enable) {
        align = enable ? TextAlignment.CENTER : TextAlignment.LEFT;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }
}
