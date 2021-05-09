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

import javax.annotation.Nullable;

import appeng.client.gui.layout.SlotGridLayout;

/**
 * Describes positioning for a slot.
 */
public class SlotPosition extends Position {

    @Nullable
    private SlotGridLayout grid;

    @Nullable
    public SlotGridLayout getGrid() {
        return grid;
    }

    public void setGrid(@Nullable SlotGridLayout grid) {
        this.grid = grid;
    }

    @Override
    public String toString() {
        String result = super.toString();
        return grid != null ? (result + "grid=" + grid) : result;
    }
}
