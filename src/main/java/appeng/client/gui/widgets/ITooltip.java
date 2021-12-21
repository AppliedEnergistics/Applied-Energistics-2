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

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

/**
 * AEBaseGui controlled Tooltip Interface.
 */
public interface ITooltip {

    /**
     * Returns the tooltip message.
     *
     * @return tooltip message or an empty list to not show a tooltip
     */

    default List<Component> getTooltipMessage() {
        return Collections.emptyList();
    }

    /**
     * Rectangle in screen relative coordinates which triggers the toooltip.
     */
    Rect2i getTooltipArea();

    /**
     * @return true if button being drawn
     */
    boolean isTooltipAreaVisible();
}
