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

import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.FuzzyMode;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.AdaptorItemHandlerPlayerInv;

/**
 * Universal Facade for other inventories. Used to conveniently interact with various types of inventories. This is not
 * used for actually monitoring an inventory. It is just for insertion and extraction, and is primarily used by
 * import/export buses.
 */
public abstract class InventoryAdaptor {
    public static InventoryAdaptor getAdaptor(final BlockEntity te, final Direction d) {
        if (te != null) {
            LazyOptional<IItemHandler> cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d);

            // Attempt getting an IItemHandler for the given side via caps
            if (cap.isPresent()) {
                return new AdaptorItemHandler(cap.orElseThrow(IllegalStateException::new));
            }
        }
        return null;
    }

    public static InventoryAdaptor getAdaptor(final Player te) {
        if (te != null) {
            return new AdaptorItemHandlerPlayerInv(te);
        }
        return null;
    }

    /**
     * Extract items.
     *
     * @param amount      How much to extract at most.
     * @param filter      Empty to match anything, otherwise the extracted item will match the stack.
     * @param destination Null to match anything, otherwise the extracted item will match this predicate.
     * @return What was extracted.
     */
    public abstract ItemStack removeItems(int amount, ItemStack filter, @Nullable Predicate<ItemStack> destination);

    /**
     * Like {@link #removeItems} but only simulating.
     */
    public abstract ItemStack simulateRemove(int amount, ItemStack filter, @Nullable Predicate<ItemStack> destination);

    /**
     * Like {@link #removeItems} but with fuzzy support.
     */
    public abstract ItemStack removeSimilarItems(int amount, ItemStack filter, FuzzyMode fuzzyMode,
            @Nullable Predicate<ItemStack> destination);

    /**
     * Like {@link #simulateRemove} but with fuzzy support.
     */
    public abstract ItemStack simulateSimilarRemove(int amount, ItemStack filter, FuzzyMode fuzzyMode,
            @Nullable Predicate<ItemStack> destination);

    /**
     * Insert items.
     * 
     * @param toBeAdded What to inesrt. Won't be mutated.
     * @return The leftover.
     */
    public abstract ItemStack addItems(ItemStack toBeAdded);

    /**
     * Like {@link #addItems} but only simulating.
     */
    public abstract ItemStack simulateAdd(ItemStack toBeSimulated);

    public abstract boolean hasSlots();
}
