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

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.inventories.BaseInternalInventory;
import appeng.api.inventories.InternalInventory;
import appeng.blockentity.AEBaseBlockEntity;

public class ItemGenBlockEntity extends AEBaseBlockEntity {

    private static final Queue<ItemStack> SHARED_POSSIBLE_ITEMS = new ArrayDeque<>();

    private final InternalInventory handler = new QueuedItemHandler();

    private Item filter = Items.AIR;
    private final Queue<ItemStack> possibleItems = new ArrayDeque<>();

    public ItemGenBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        if (SHARED_POSSIBLE_ITEMS.isEmpty()) {
            for (final Item item : Registry.ITEM) {
                addPossibleItem(item, SHARED_POSSIBLE_ITEMS);
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        data.putString("filter", Registry.ITEM.getKey(filter).toString());
    }

    @Override
    public void loadTag(CompoundTag data) {
        if (data.contains("filter")) {
            Item item = Registry.ITEM.get(new ResourceLocation(data.getString("filter")));
            this.setItem(item);
        }
        super.loadTag(data);
    }

    public Storage<ItemVariant> getItemHandler() {
        return this.handler.toStorage();
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

        if (item.canBeDepleted()) {
            ItemStack sampleStack = new ItemStack(item);
            int maxDamage = sampleStack.getMaxDamage();
            for (int dmg = 0; dmg < maxDamage; dmg++) {
                ItemStack is = sampleStack.copy();
                is.setDamageValue(dmg);
                queue.add(is);
            }
        } else if (item.getItemCategory() != null) {
            final NonNullList<ItemStack> list = NonNullList.create();
            item.fillItemCategory(item.getItemCategory(), list);
            queue.addAll(list);
        }
    }

    class QueuedItemHandler extends BaseInternalInventory {
        @Override
        public void setItemDirect(int slotIndex, @Nonnull ItemStack stack) {
        }

        @Override
        @Nonnull
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        @Nonnull
        public ItemStack getStackInSlot(int slot) {
            return getPossibleItems().peek() != null ? getPossibleItems().peek().copy() : ItemStack.EMPTY;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return false;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        @Nonnull
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            final ItemStack is = getPossibleItems().peek();

            if (is == null) {
                return ItemStack.EMPTY;
            }

            return simulate ? is.copy() : this.getNextItem();
        }

        private ItemStack getNextItem() {
            var is = getPossibleItems().poll();

            getPossibleItems().add(is);
            return is.copy();
        }
    }
}
