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

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

import appeng.blockentity.AEBaseBlockEntity;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;

public class ItemGenBlockEntity extends AEBaseBlockEntity implements InternalInventoryHost {

    private static final Queue<ItemStack> SHARED_POSSIBLE_ITEMS = new ArrayDeque<>();

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 16, 64);

    private Item filter = Items.AIR;

    private final Queue<ItemStack> possibleItems = new ArrayDeque<>();

    public ItemGenBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (SHARED_POSSIBLE_ITEMS.isEmpty()) {
            initGlobalPossibleItems();
        }
        refillInv();
    }

    private synchronized static void initGlobalPossibleItems() {
        if (SHARED_POSSIBLE_ITEMS.isEmpty()) {
            for (Item item : BuiltInRegistries.ITEM) {
                addPossibleItem(item, SHARED_POSSIBLE_ITEMS);
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        data.putString("filter", BuiltInRegistries.ITEM.getKey(filter).toString());
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        if (data.contains("filter")) {
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(data.getString("filter")));
            this.setItem(item);
        }
        super.loadTag(data, registries);
    }

    public IItemHandler getItemHandler() {
        return inv.toItemHandler();
    }

    public void setItem(Item item) {
        this.filter = item;
        this.possibleItems.clear();

        addPossibleItem(this.filter, this.possibleItems);
        refillInv();
    }

    private Queue<ItemStack> getPossibleItems() {
        return this.filter != Items.AIR ? this.possibleItems : SHARED_POSSIBLE_ITEMS;
    }

    private static void addPossibleItem(Item item, Queue<ItemStack> queue) {
        if (item == null || item == Items.AIR) {
            return;
        }

        ItemStack sampleStack = item.getDefaultInstance();
        if (sampleStack.isDamageableItem()) {
            int maxDamage = sampleStack.getMaxDamage();
            for (int dmg = 0; dmg < maxDamage; dmg++) {
                ItemStack is = sampleStack.copy();
                is.setDamageValue(dmg);
                queue.add(is);
            }
        } else {
            queue.add(item.getDefaultInstance());
        }
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        saveChanges();
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        if (inv.getStackInSlot(slot).isEmpty()) {
            refillSlot(slot);
        }
    }

    private void refillInv() {
        for (int slot = 0; slot < inv.size(); slot++) {
            refillSlot(slot);
        }
    }

    private void refillSlot(int slot) {
        var stack = getPossibleItems().poll();
        if (stack != null) {
            var copy = stack.copy();
            copy.setCount(stack.getMaxStackSize());
            inv.setItemDirect(slot, copy);
            getPossibleItems().add(stack);
        }
    }
}
