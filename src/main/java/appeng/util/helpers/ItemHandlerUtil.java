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


import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;


public class ItemHandlerUtil {
    private ItemHandlerUtil() {
    }

    public static void setStackInSlot(final IItemHandler inv, final int slot, final ItemStack stack) {
        if (inv instanceof IItemHandlerModifiable) {
            ((IItemHandlerModifiable) inv).setStackInSlot(slot, stack);
        } else {
            inv.extractItem(slot, Integer.MAX_VALUE, false);
            inv.insertItem(slot, stack, false);
        }
    }

    public static void clear(final IItemHandler inv) {
        for (int x = 0; x < inv.getSlots(); x++) {
            setStackInSlot(inv, x, ItemStack.EMPTY);
        }
    }

    public static boolean isEmpty(final IItemHandler inv) {
        for (int x = 0; x < inv.getSlots(); x++) {
            if (!inv.getStackInSlot(x).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static void copy(final IItemHandler from, final IItemHandler to, boolean deepCopy) {
        for (int i = 0; i < Math.min(from.getSlots(), to.getSlots()); ++i) {
            setStackInSlot(to, i, deepCopy ? from.getStackInSlot(i).copy() : from.getStackInSlot(i));
        }
    }

    public static void copy(final InventoryCrafting from, final IItemHandler to, boolean deepCopy) {
        for (int i = 0; i < Math.min(from.getSizeInventory(), to.getSlots()); ++i) {
            setStackInSlot(to, i, deepCopy ? from.getStackInSlot(i).copy() : from.getStackInSlot(i));
        }
    }
}
