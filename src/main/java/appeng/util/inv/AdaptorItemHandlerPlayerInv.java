/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.util.inv;


import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;


public class AdaptorItemHandlerPlayerInv extends AdaptorItemHandler {
    public AdaptorItemHandlerPlayerInv(final EntityPlayer playerInv) {
        super(new PlayerMainInvWrapper(playerInv.inventory));
    }

    /**
     * Tries to fill existing stacks first
     */
    @Override
    protected ItemStack addItems(final ItemStack itemsToAdd, final boolean simulate) {
        if (itemsToAdd.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack left = itemsToAdd.copy();

        for (int slot = 0; slot < this.itemHandler.getSlots(); slot++) {
            ItemStack is = this.itemHandler.getStackInSlot(slot);

            if (Platform.itemComparisons().isSameItem(is, left)) {
                left = this.itemHandler.insertItem(slot, left, simulate);
            }
            if (left.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        for (int slot = 0; slot < this.itemHandler.getSlots(); slot++) {
            left = this.itemHandler.insertItem(slot, left, simulate);
            if (left.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        return left;
    }

}
