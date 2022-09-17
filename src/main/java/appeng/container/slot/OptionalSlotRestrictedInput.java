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

package appeng.container.slot;


import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandler;


public class OptionalSlotRestrictedInput extends SlotRestrictedInput {

    private final int groupNum;
    private final IOptionalSlotHost host;

    public OptionalSlotRestrictedInput(final PlacableItemType valid, final IItemHandler i, final IOptionalSlotHost host, final int slotIndex, final int x, final int y, final int grpNum, final InventoryPlayer invPlayer) {
        super(valid, i, slotIndex, x, y, invPlayer);
        this.groupNum = grpNum;
        this.host = host;
    }

    @Override
    public boolean isSlotEnabled() {
        if (this.host == null) {
            return false;
        }

        return this.host.isSlotEnabled(this.groupNum);
    }
}
