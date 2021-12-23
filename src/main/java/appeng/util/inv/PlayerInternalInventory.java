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

package appeng.util.inv;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import appeng.api.inventories.InternalInventory;

/**
 * Exposes the main player inventory and hotbar as an {@link InternalInventory}.
 */
public class PlayerInternalInventory implements InternalInventory {
    private final Inventory inventory;

    public PlayerInternalInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public int size() {
        return Inventory.INVENTORY_SIZE;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return inventory.getItem(slotIndex);
    }

    @Override
    public void setItemDirect(int slotIndex, ItemStack stack) {
        inventory.setItem(slotIndex, stack);
        if (!stack.isEmpty()) {
            inventory.getItem(slotIndex).setPopTime(5);
        }
    }
}
