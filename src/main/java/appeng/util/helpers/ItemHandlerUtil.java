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

package appeng.util.helpers;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import appeng.api.inventories.InternalInventory;

public class ItemHandlerUtil {
    private ItemHandlerUtil() {
    }

    public static void clear(final InternalInventory inv) {
        for (int x = 0; x < inv.size(); x++) {
            inv.setItemDirect(x, ItemStack.EMPTY);
        }
    }

    public static boolean isEmpty(IItemHandler inv) {
        for (int x = 0; x < inv.getSlots(); x++) {
            if (!inv.getStackInSlot(x).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static void copy(final CraftingContainer from, final InternalInventory to, boolean deepCopy) {
        for (int i = 0; i < Math.min(from.getContainerSize(), to.size()); ++i) {
            to.setItemDirect(i, deepCopy ? from.getItem(i).copy() : from.getItem(i));
        }
    }
}
