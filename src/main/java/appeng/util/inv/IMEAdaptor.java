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

package appeng.util.inv;

import java.util.Iterator;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.Api;
import appeng.util.InventoryAdaptor;
import appeng.util.item.AEItemStack;

public class IMEAdaptor extends InventoryAdaptor {

    private final IMEInventory<IAEItemStack> target;
    private final IActionSource src;
    private int maxSlots = 0;

    public IMEAdaptor(final IMEInventory<IAEItemStack> input, final IActionSource src) {
        this.target = input;
        this.src = src;
    }

    @Override
    public boolean hasSlots() {
        return true;
    }

    @Override
    public Iterator<ItemSlot> iterator() {
        return new IMEAdaptorIterator(this, this.getList());
    }

    private IItemList<IAEItemStack> getList() {
        return this.target
                .getAvailableItems(Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createList());
    }

    @Override
    public ItemStack removeItems(final int amount, final ItemStack filter, final IInventoryDestination destination) {
        return this.doRemoveItems(amount, filter, destination, Actionable.MODULATE);
    }

    private ItemStack doRemoveItems(final int amount, final ItemStack filter, final IInventoryDestination destination,
            final Actionable type) {
        IAEItemStack req = null;

        if (filter.isEmpty()) {
            final IItemList<IAEItemStack> list = this.getList();
            if (!list.isEmpty()) {
                req = list.getFirstItem();
            }
        } else {
            req = AEItemStack.fromItemStack(filter);
        }

        IAEItemStack out = null;

        if (req != null) {
            req.setStackSize(amount);
            out = this.target.extractItems(req, type, this.src);
        }

        if (out != null) {
            return out.createItemStack();
        }

        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack simulateRemove(final int amount, final ItemStack filter, final IInventoryDestination destination) {
        return this.doRemoveItems(amount, filter, destination, Actionable.SIMULATE);
    }

    @Override
    public ItemStack removeSimilarItems(final int amount, final ItemStack filter, final FuzzyMode fuzzyMode,
            final IInventoryDestination destination) {
        if (filter.isEmpty()) {
            return this.doRemoveItems(amount, null, destination, Actionable.MODULATE);
        }
        return this.doRemoveItemsFuzzy(amount, filter, destination, Actionable.MODULATE, fuzzyMode);
    }

    private ItemStack doRemoveItemsFuzzy(final int amount, final ItemStack filter,
            final IInventoryDestination destination, final Actionable type, final FuzzyMode fuzzyMode) {
        final IAEItemStack reqFilter = AEItemStack.fromItemStack(filter);
        if (reqFilter == null) {
            return ItemStack.EMPTY;
        }

        IAEItemStack out = null;

        for (final IAEItemStack req : ImmutableList.copyOf(this.getList().findFuzzy(reqFilter, fuzzyMode))) {
            if (req != null) {
                req.setStackSize(amount);
                out = this.target.extractItems(req, type, this.src);
                if (out != null) {
                    return out.createItemStack();
                }
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack simulateSimilarRemove(final int amount, final ItemStack filter, final FuzzyMode fuzzyMode,
            final IInventoryDestination destination) {
        if (filter.isEmpty()) {
            return this.doRemoveItems(amount, ItemStack.EMPTY, destination, Actionable.SIMULATE);
        }
        return this.doRemoveItemsFuzzy(amount, filter, destination, Actionable.SIMULATE, fuzzyMode);
    }

    @Override
    public ItemStack addItems(final ItemStack toBeAdded) {
        final IAEItemStack in = AEItemStack.fromItemStack(toBeAdded);
        if (in != null) {
            final IAEItemStack out = this.target.injectItems(in, Actionable.MODULATE, this.src);
            if (out != null) {
                return out.createItemStack();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack simulateAdd(final ItemStack toBeSimulated) {
        final IAEItemStack in = AEItemStack.fromItemStack(toBeSimulated);
        if (in != null) {
            final IAEItemStack out = this.target.injectItems(in, Actionable.SIMULATE, this.src);
            if (out != null) {
                return out.createItemStack();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean containsItems() {
        return !this.getList().isEmpty();
    }

    int getMaxSlots() {
        return this.maxSlots;
    }

    void setMaxSlots(final int maxSlots) {
        this.maxSlots = maxSlots;
    }
}
