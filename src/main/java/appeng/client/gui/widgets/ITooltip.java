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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

import appeng.client.gui.Tooltip;

/**
 * AEBaseGui controlled Tooltip Interface.
 */
public interface ITooltip {

    /**
     * Returns the tooltip message.
     * <p>
     * Should use {@link StringTextComponent#EMPTY} for no tooltip
     *
     * @return tooltip message
     */
    @Nonnull
    ITextComponent getTooltipMessage();

    /**
     * x Location for the object that triggers the tooltip.
     *
     * @return xPosition
     */
    int getTooltipAreaX();

    /**
     * y Location for the object that triggers the tooltip.
     *
     * @return yPosition
     */
    int getTooltipAreaY();

    /**
     * Width of the object that triggers the tooltip.
     *
     * @return width
     */
    int getTooltipAreaWidth();

    /**
     * Height for the object that triggers the tooltip.
     *
     * @return height
     */
    int getTooltipAreaHeight();

    /**
     * @return true if button being drawn
     */
    boolean isTooltipAreaVisible();

    @Nullable
    default Tooltip getTooltip(int mouseX, int mouseY) {
        if (!isTooltipAreaVisible()) {
            return null;
        }

        int x = getTooltipAreaX();
        int y = getTooltipAreaY();

        if (x < mouseX && x + getTooltipAreaWidth() > mouseX
                && y < mouseY && y + getTooltipAreaHeight() > mouseY) {
            List<ITextComponent> content = new ArrayList<>();
            getTooltipMessage().getComponentWithStyle((style, text) -> {
                for (String line : text.split("\n")) {
                    IFormattableTextComponent textLine = new StringTextComponent(line).mergeStyle(style);
                    if (content.isEmpty()) {
                        textLine.mergeStyle(TextFormatting.WHITE);
                    } else {
                        textLine.mergeStyle(TextFormatting.GRAY);
                    }
                    content.add(textLine);
                }
                return Optional.empty();
            }, Style.EMPTY);

            Rectangle2d anchorBounds = new Rectangle2d(getTooltipAreaX(), getTooltipAreaY(),
                    getTooltipAreaWidth(), getTooltipAreaHeight());
            return new Tooltip(content);
        }

        return null;
    }

}
