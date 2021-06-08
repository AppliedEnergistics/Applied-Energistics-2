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

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import alexiil.mc.lib.attributes.AttributeList;
import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.ItemExtractable;
import alexiil.mc.lib.attributes.item.filter.ExactItemStackFilter;

import appeng.tile.AEBaseTileEntity;

public class ItemGenTileEntity extends AEBaseTileEntity {

    private static final Queue<ItemStack> SHARED_POSSIBLE_ITEMS = new ArrayDeque<>();

    private final QueuedItemHandler handler = new QueuedItemHandler();

    private Item filter = Items.AIR;
    private final Queue<ItemStack> possibleItems = new ArrayDeque<>();

    public ItemGenTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        if (SHARED_POSSIBLE_ITEMS.isEmpty()) {
            for (final Item item : Registry.ITEM) {
                addPossibleItem(item, SHARED_POSSIBLE_ITEMS);
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT data) {
        data.putString("filter", Registry.ITEM.getKey(filter).toString());
        return super.write(data);
    }

    @Override
    public void read(BlockState blockState, CompoundNBT data) {
        if (data.contains("filter")) {
            Item item = Registry.ITEM.getOrDefault(new ResourceLocation(data.getString("filter")));
            this.setItem(item);
        }
        super.read(blockState, data);
    }

    @Override
    public void addAllAttributes(World world, BlockPos pos, BlockState state, AttributeList<?> to) {
        to.offer(handler);
    }

    public void setItem(Item item) {
        this.filter = item;
        this.possibleItems.clear();

        addPossibleItem(this.filter, this.possibleItems);
    }

    private Queue<ItemStack> getPossibleItems() {
        return this.filter != Items.AIR ? this.possibleItems : SHARED_POSSIBLE_ITEMS;
    }

    private static void addPossibleItem(Item item, Queue<ItemStack> queue) {
        if (item == null || item == Items.AIR) {
            return;
        }

        if (item.isDamageable()) {
            ItemStack sampleStack = new ItemStack(item);
            int maxDamage = sampleStack.getMaxDamage();
            for (int dmg = 0; dmg < maxDamage; dmg++) {
                ItemStack is = sampleStack.copy();
                is.setDamage(dmg);
                queue.add(is);
            }
        } else if (item.getGroup() != null) {
            final NonNullList<ItemStack> list = NonNullList.create();
            item.fillItemGroup(item.getGroup(), list);
            queue.addAll(list);
        }
    }

    class QueuedItemHandler implements FixedItemInv {

        @Override
        public boolean setInvStack(int i, ItemStack itemStack, Simulation simulation) {
            if (itemStack.isEmpty()) {
                getNextItem(); // Go to next item
            }
            return itemStack.isEmpty();
        }

        @Override
        @Nonnull
        public ItemStack getInvStack(int slot) {
            return getPossibleItems().peek() != null ? getPossibleItems().peek().copy() : ItemStack.EMPTY;
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack) {
            return false;
        }

        @Override
        public int getSlotCount() {
            return 1;
        }

        @Override
        public ItemExtractable getExtractable() {
            return (itemFilter, i, simulation) -> {
                // When item filter asks for a specific item, just return that
                if (itemFilter instanceof ExactItemStackFilter) {
                    return ((ExactItemStackFilter) itemFilter).stack.copy();
                }

                final ItemStack is = getPossibleItems().peek();

                if (is == null || !itemFilter.matches(is)) {
                    return ItemStack.EMPTY;
                }

                return simulation == Simulation.SIMULATE ? is.copy() : this.getNextItem();
            };
        }

        private ItemStack getNextItem() {
            final ItemStack is = getPossibleItems().poll();

            getPossibleItems().add(is);
            return is.copy();
        }
    }
}
