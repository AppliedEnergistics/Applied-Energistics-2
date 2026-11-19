/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2026, TeamAppliedEnergistics, All rights reserved.
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

import net.minecraft.resources.Identifier;

import appeng.core.AppEng;

/**
 * Describes how the background of the slots in a group of slots is drawn.
 */
public class SlotBackground {
    public static final Identifier DEFAULT_SPRITE = AppEng.makeId("slot");
    public static final Identifier BORDER_SPRITE = AppEng.makeId("slot_border");

    public static final SlotBackground DEFAULT = new SlotBackground();

    /**
     * The sprite drawn behind each slot. It is centered on the 16x16 item area of the slot. By convention, slot
     * background sprites have a transparent margin of 1 pixel, which is covered by the border.
     */
    private Identifier sprite = DEFAULT_SPRITE;

    /**
     * Whether a background is drawn for the slots at all.
     */
    private boolean visible = true;

    /**
     * Whether a border ({@link #BORDER_SPRITE}) is drawn around contiguous groups of slots.
     */
    private boolean border = true;

    /**
     * Creates a slot background using a custom sprite.
     */
    public static SlotBackground of(Identifier sprite) {
        var result = new SlotBackground();
        result.sprite = sprite;
        return result;
    }

    public Identifier getSprite() {
        return sprite;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean hasBorder() {
        return border;
    }
}
