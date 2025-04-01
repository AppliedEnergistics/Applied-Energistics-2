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

package appeng.client.gui;

import net.minecraft.client.renderer.Rect2i;

/**
 * Utility class for dealing with immutable {@link Rect2i}.
 */
public final class Rects {

    public static final Rect2i ZERO = new Rect2i(0, 0, 0, 0);

    private Rects() {
    }

    public static Rect2i expand(Rect2i rect, int amount) {
        return new Rect2i(
                rect.getX() - amount,
                rect.getY() - amount,
                rect.getWidth() + 2 * amount,
                rect.getHeight() + 2 * amount);
    }

    public static Rect2i move(Rect2i rect, int x, int y) {
        return new Rect2i(
                rect.getX() + x,
                rect.getY() + y,
                rect.getWidth(),
                rect.getHeight());
    }

}
