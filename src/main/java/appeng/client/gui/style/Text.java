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
     * Center on the computed x position.
     */
    private boolean centerHorizontally;

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

    public boolean isCenterHorizontally() {
        return centerHorizontally;
    }

    public void setCenterHorizontally(boolean centerHorizontally) {
        this.centerHorizontally = centerHorizontally;
    }
}
