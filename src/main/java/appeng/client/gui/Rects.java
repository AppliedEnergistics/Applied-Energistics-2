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

import net.minecraft.client.renderer.Rectangle2d;

/**
 * Utility class for dealing with immutable {@link Rectangle2d}.
 */
public final class Rects {

    public static final Rectangle2d ZERO = new Rectangle2d(0, 0, 0, 0);

    private Rects() {
    }

    public static Rectangle2d expand(Rectangle2d rect, int amount) {
        return new Rectangle2d(
                rect.getX() - amount,
                rect.getY() - amount,
                rect.getWidth() + 2 * amount,
                rect.getHeight() + 2 * amount);
    }

    public static Rectangle2d move(Rectangle2d rect, int x, int y) {
        return new Rectangle2d(
                rect.getX() + x,
                rect.getY() + y,
                rect.getWidth(),
                rect.getHeight());
    }

}
