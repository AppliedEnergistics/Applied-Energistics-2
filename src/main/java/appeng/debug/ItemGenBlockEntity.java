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

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import alexiil.mc.lib.attributes.AttributeList;
import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.ItemExtractable;
import alexiil.mc.lib.attributes.item.filter.ExactItemStackFilter;

import appeng.tile.AEBaseBlockEntity;

public class ItemGenBlockEntity extends AEBaseBlockEntity {

    private static final Queue<ItemStack> POSSIBLE_ITEMS = new ArrayDeque<>();

    private final QueuedItemHandler handler = new QueuedItemHandler();

    public ItemGenBlockEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        if (POSSIBLE_ITEMS.isEmpty()) {
            for (final Item mi : Registry.ITEM) {
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

                        final NonNullList<ItemStack> list = NonNullList.create();
                        mi.fillItemGroup(mi.getGroup(), list);
                        POSSIBLE_ITEMS.addAll(list);
                    }
                }
            }
        }
    }

    @Override
    public void addAllAttributes(World world, BlockPos pos, BlockState state, AttributeList<?> to) {
        to.offer(handler);
    }

    static class QueuedItemHandler implements FixedItemInv {

        @Override
        public ItemExtractable getExtractable() {
            return (itemFilter, i, simulation) -> {
                // When item filter asks for a specific item, just return that
                if (itemFilter instanceof ExactItemStackFilter) {
                    return ((ExactItemStackFilter) itemFilter).stack.copy();
                }

                final ItemStack is = POSSIBLE_ITEMS.peek();

                if (is == null || !itemFilter.matches(is)) {
                    return ItemStack.EMPTY;
                }

                return simulation == Simulation.SIMULATE ? is.copy() : getNextItem();
            };
        }

        private ItemStack getNextItem() {
            final ItemStack is = POSSIBLE_ITEMS.poll();

            POSSIBLE_ITEMS.add(is);
            return is.copy();
        }

        @Override
        public int getSlotCount() {
            return 1;
        }

        @Override
        public ItemStack getInvStack(int i) {
            return POSSIBLE_ITEMS.peek().copy();
        }

        @Override
        public boolean isItemValidForSlot(int i, ItemStack itemStack) {
            return false;
        }

        @Override
        public boolean setInvStack(int i, ItemStack itemStack, Simulation simulation) {
            if (itemStack.isEmpty()) {
                getNextItem(); // Go to next item
            }
            return itemStack.isEmpty();
        }
    }
}
