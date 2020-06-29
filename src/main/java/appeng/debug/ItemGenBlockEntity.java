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

import java.util.ArrayDeque;
import java.util.Queue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.tile.AEBaseBlockEntity;

public class ItemGenBlockEntity extends AEBaseBlockEntity {

    private static final Queue<ItemStack> POSSIBLE_ITEMS = new ArrayDeque<>();

    private final FixedItemInv handler = new QueuedItemHandler();

    public ItemGenBlockEntity(BlockEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        if (POSSIBLE_ITEMS.isEmpty()) {
            for (final Item mi : ForgeRegistries.ITEMS) {
                if (mi != null && mi != Items.AIR) {
                    if (mi.isDamageable()) {
                        ItemStack sampleStack = new ItemStack(mi);
                        int maxDamage = sampleStack.getMaxDamage();
                        for (int dmg = 0; dmg < maxDamage; dmg++) {
                            ItemStack is = sampleStack.copy();
                            is.setDamage(dmg);
                            POSSIBLE_ITEMS.add(is);
                        }
                    } else {
                        if (mi.getGroup() == null) {
                            continue;
                        }

                        final DefaultedList<ItemStack> list = DefaultedList.create();
                        mi.appendStacks(mi.getGroup(), list);
                        POSSIBLE_ITEMS.addAll(list);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (CapabilityItemHandler.ITEM_HANDLER_CAPABILITY == capability) {
            return (LazyOptional<T>) LazyOptional.of(() -> this.handler);
        }
        return super.getCapability(capability, facing);
    }

    class QueuedItemHandler implements FixedItemInv {

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
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return false;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public int getMaxAmount(int slot, ItemStack stack) {
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
    };
}
