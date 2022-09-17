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

package appeng.debug;


import appeng.tile.AEBaseTile;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Queue;


public class TileItemGen extends AEBaseTile {

    private static final Queue<ItemStack> POSSIBLE_ITEMS = new ArrayDeque<>();

    private final IItemHandler handler = new QueuedItemHandler();

    public TileItemGen() {
        if (POSSIBLE_ITEMS.isEmpty()) {
            for (final Object obj : Item.REGISTRY) {
                final Item mi = (Item) obj;
                if (mi != null && mi != Items.AIR) {
                    if (mi.isDamageable()) {
                        for (int dmg = 0; dmg < mi.getMaxDamage(); dmg++) {
                            POSSIBLE_ITEMS.add(new ItemStack(mi, 1, dmg));
                        }
                    } else {
                        final NonNullList<ItemStack> list = NonNullList.create();
                        mi.getSubItems(mi.getCreativeTab(), list);
                        POSSIBLE_ITEMS.addAll(list);
                    }
                }
            }
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (CapabilityItemHandler.ITEM_HANDLER_CAPABILITY == capability) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (CapabilityItemHandler.ITEM_HANDLER_CAPABILITY == capability) {
            return (T) this.handler;
        }
        return super.getCapability(capability, facing);
    }

    class QueuedItemHandler implements IItemHandler {

        @Override
        @Nonnull
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        @Nonnull
        public ItemStack getStackInSlot(int slot) {
            return POSSIBLE_ITEMS.peek() != null ? POSSIBLE_ITEMS.peek().copy() : ItemStack.EMPTY;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        @Nonnull
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            final ItemStack is = POSSIBLE_ITEMS.peek();

            if (is == null) {
                return ItemStack.EMPTY;
            }

            return simulate ? is.copy() : this.getNextItem();
        }

        private ItemStack getNextItem() {
            final ItemStack is = POSSIBLE_ITEMS.poll();

            POSSIBLE_ITEMS.add(is);
            return is.copy();
        }
    }

}
