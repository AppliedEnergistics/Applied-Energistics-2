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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;

import appeng.client.gui.Tooltip;

/**
 * AEBaseGui controlled Tooltip Interface.
 */
public interface ITooltip {

    /**
     * Returns the tooltip message.
     *
     * @return tooltip message or an empty list to not show a tooltip
     */
    @Nonnull
    default List<net.minecraft.network.chat.Component> getTooltipMessage() {
        return Collections.emptyList();
    }

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

            List<net.minecraft.network.chat.Component> lines = getTooltipMessage();

            // Don't show empty tooltips
            if (lines.isEmpty()) {
                return null;
            }

            return new Tooltip(lines);
        }

        return null;
    }

}
