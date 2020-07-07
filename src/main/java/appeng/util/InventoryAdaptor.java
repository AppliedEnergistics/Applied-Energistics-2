/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.util;

import alexiil.mc.lib.attributes.SearchOptions;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.ItemAttributes;
import appeng.api.config.FuzzyMode;
import appeng.util.inv.AdaptorFixedInv;
import appeng.util.inv.AdaptorItemHandlerPlayerInv;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.ItemSlot;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

/**
 * Universal Facade for other inventories. Used to conveniently interact with
 * various types of inventories. This is not used for actually monitoring an
 * inventory. It is just for insertion and extraction, and is primarily used by
 * import/export buses.
 */
public abstract class InventoryAdaptor implements Iterable<ItemSlot> {
    public static InventoryAdaptor getAdaptor(final BlockEntity te, final Direction d) {
        FixedItemInv inv = ItemAttributes.FIXED_INV.get(te.getWorld(), te.getPos().offset(d), SearchOptions.inDirection(d.getOpposite()));

        if (inv == ItemAttributes.FIXED_INV.defaultValue) {
            return null;
        }

        return new AdaptorFixedInv(inv);
    }

    public static InventoryAdaptor getAdaptor(final PlayerEntity te) {
        if (te != null) {
            return new AdaptorItemHandlerPlayerInv(te);
        }
        return null;
    }

    // return what was extracted.
    public abstract ItemStack removeItems(int amount, ItemStack filter, IInventoryDestination destination);

    public abstract ItemStack simulateRemove(int amount, ItemStack filter, IInventoryDestination destination);

    // return what was extracted.
    public abstract ItemStack removeSimilarItems(int amount, ItemStack filter, FuzzyMode fuzzyMode,
                                                 IInventoryDestination destination);

    public abstract ItemStack simulateSimilarRemove(int amount, ItemStack filter, FuzzyMode fuzzyMode,
                                                    IInventoryDestination destination);

    // return what isn't used...
    public abstract ItemStack addItems(ItemStack toBeAdded);

    public abstract ItemStack simulateAdd(ItemStack toBeSimulated);

    public abstract boolean containsItems();

    public abstract boolean hasSlots();
}
