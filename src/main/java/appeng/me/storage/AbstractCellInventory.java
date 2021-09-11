/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.me.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.FuzzyMode;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellInventory;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;

/**
 * @author DrummerMC
 * @version rv6 - 2018-01-17
 * @since rv6 2018-01-17
 */
public abstract class AbstractCellInventory<T extends IAEStack> implements ICellInventory<T> {
    private static final int MAX_ITEM_TYPES = 63;
    private static final String ITEM_TYPE_TAG = "it";
    private static final String ITEM_COUNT_TAG = "ic";
    private static final String ITEM_SLOT = "#";
    private static final String ITEM_SLOT_COUNT = "@";
    private static final String[] ITEM_SLOT_KEYS = new String[MAX_ITEM_TYPES];
    private static final String[] ITEM_SLOT_COUNT_KEYS = new String[MAX_ITEM_TYPES];
    private final CompoundTag tagCompound;
    protected final ISaveProvider container;
    private int maxItemTypes;
    private short storedItems;
    private int storedItemCount;
    protected IItemList<T> cellItems;
    private final ItemStack i;
    protected final IStorageCell<T> cellType;
    protected final int itemsPerByte;
    private boolean isPersisted = true;

    static {
        for (int x = 0; x < MAX_ITEM_TYPES; x++) {
            ITEM_SLOT_KEYS[x] = ITEM_SLOT + x;
            ITEM_SLOT_COUNT_KEYS[x] = ITEM_SLOT_COUNT + x;
        }
    }

    protected AbstractCellInventory(final IStorageCell<T> cellType, final ItemStack o, final ISaveProvider container) {
        this.i = o;
        this.cellType = cellType;
        this.itemsPerByte = this.cellType.getChannel().getUnitsPerByte();
        this.maxItemTypes = this.cellType.getTotalTypes(this.i);

        if (this.maxItemTypes > MAX_ITEM_TYPES) {
            this.maxItemTypes = MAX_ITEM_TYPES;
        }
        if (this.maxItemTypes < 1) {
            this.maxItemTypes = 1;
        }

        this.container = container;
        this.tagCompound = o.getOrCreateTag();
        this.storedItems = this.tagCompound.getShort(ITEM_TYPE_TAG);
        this.storedItemCount = this.tagCompound.getInt(ITEM_COUNT_TAG);
        this.cellItems = null;
    }

    protected IItemList<T> getCellItems() {
        if (this.cellItems == null) {
            this.cellItems = this.getChannel().createList();
            this.loadCellItems();
        }

        return this.cellItems;
    }

    @Override
    public void persist() {
        if (this.isPersisted) {
            return;
        }

        int itemCount = 0;

        // add new pretty stuff...
        int x = 0;
        for (final T v : this.cellItems) {
            itemCount += v.getStackSize();

            final CompoundTag g = new CompoundTag();
            v.writeToNBT(g);
            this.tagCompound.put(ITEM_SLOT_KEYS[x], g);
            this.tagCompound.putInt(ITEM_SLOT_COUNT_KEYS[x], (int) v.getStackSize());

            x++;
        }

        final short oldStoredItems = this.storedItems;

        this.storedItems = (short) this.cellItems.size();
        if (this.cellItems.isEmpty()) {
            this.tagCompound.remove(ITEM_TYPE_TAG);
        } else {
            this.tagCompound.putShort(ITEM_TYPE_TAG, this.storedItems);
        }

        this.storedItemCount = itemCount;
        if (itemCount == 0) {
            this.tagCompound.remove(ITEM_COUNT_TAG);
        } else {
            this.tagCompound.putInt(ITEM_COUNT_TAG, itemCount);
        }

        // clean any old crusty stuff...
        for (; x < oldStoredItems && x < this.maxItemTypes; x++) {
            this.tagCompound.remove(ITEM_SLOT_KEYS[x]);
            this.tagCompound.remove(ITEM_SLOT_COUNT_KEYS[x]);
        }

        this.isPersisted = true;
    }

    protected void saveChanges() {
        // recalculate values
        this.storedItems = (short) this.cellItems.size();
        this.storedItemCount = 0;
        for (final T v : this.cellItems) {
            this.storedItemCount += v.getStackSize();
        }

        this.isPersisted = false;
        if (this.container != null) {
            this.container.saveChanges(this);
        } else {
            // if there is no ISaveProvider, store to NBT immediately
            this.persist();
        }
    }

    private void loadCellItems() {
        if (this.cellItems == null) {
            this.cellItems = this.getChannel().createList();
        }

        this.cellItems.resetStatus(); // clears totals and stuff.

        final int types = (int) this.getStoredItemTypes();
        boolean needsUpdate = false;

        for (int slot = 0; slot < types; slot++) {
            CompoundTag compoundTag = this.tagCompound.getCompound(ITEM_SLOT_KEYS[slot]);
            int stackSize = this.tagCompound.getInt(ITEM_SLOT_COUNT_KEYS[slot]);
            needsUpdate |= !this.loadCellItem(compoundTag, stackSize);
        }

        if (needsUpdate) {
            this.saveChanges();
        }
    }

    /**
     * Load a single item.
     * 
     * @return true when successfully loaded
     */
    protected abstract boolean loadCellItem(CompoundTag compoundTag, int stackSize);

    @Override
    public IItemList<T> getAvailableItems(final IItemList<T> out) {
        for (final T item : this.getCellItems()) {
            out.add(item);
        }

        return out;
    }

    @Override
    public ItemStack getItemStack() {
        return this.i;
    }

    @Override
    public double getIdleDrain() {
        return this.cellType.getIdleDrain();
    }

    @Override
    public FuzzyMode getFuzzyMode() {
        return this.cellType.getFuzzyMode(this.i);
    }

    @Override
    public IItemHandler getConfigInventory() {
        return this.cellType.getConfigInventory(this.i);
    }

    @Override
    public IItemHandler getUpgradesInventory() {
        return this.cellType.getUpgradesInventory(this.i);
    }

    @Override
    public int getBytesPerType() {
        return this.cellType.getBytesPerType(this.i);
    }

    @Override
    public boolean canHoldNewItem() {
        final long bytesFree = this.getFreeBytes();
        return (bytesFree > this.getBytesPerType()
                || bytesFree == this.getBytesPerType() && this.getUnusedItemCount() > 0)
                && this.getRemainingItemTypes() > 0;
    }

    @Override
    public long getTotalBytes() {
        return this.cellType.getBytes(this.i);
    }

    @Override
    public long getFreeBytes() {
        return this.getTotalBytes() - this.getUsedBytes();
    }

    @Override
    public long getTotalItemTypes() {
        return this.maxItemTypes;
    }

    @Override
    public long getStoredItemCount() {
        return this.storedItemCount;
    }

    @Override
    public long getStoredItemTypes() {
        return this.storedItems;
    }

    @Override
    public long getRemainingItemTypes() {
        final long basedOnStorage = this.getFreeBytes() / this.getBytesPerType();
        final long baseOnTotal = this.getTotalItemTypes() - this.getStoredItemTypes();
        return basedOnStorage > baseOnTotal ? baseOnTotal : basedOnStorage;
    }

    @Override
    public long getUsedBytes() {
        final long bytesForItemCount = (this.getStoredItemCount() + this.getUnusedItemCount()) / this.itemsPerByte;
        return this.getStoredItemTypes() * this.getBytesPerType() + bytesForItemCount;
    }

    @Override
    public long getRemainingItemCount() {
        final long remaining = this.getFreeBytes() * this.itemsPerByte + this.getUnusedItemCount();
        return remaining > 0 ? remaining : 0;
    }

    @Override
    public int getUnusedItemCount() {
        final int div = (int) (this.getStoredItemCount() % 8);

        if (div == 0) {
            return 0;
        }

        return this.itemsPerByte - div;
    }

    @Override
    public CellState getStatusForCell() {
        if (this.getStoredItemTypes() == 0) {
            return CellState.EMPTY;
        }
        if (this.canHoldNewItem()) {
            return CellState.NOT_EMPTY;
        }
        if (this.getRemainingItemCount() > 0) {
            return CellState.TYPES_FULL;
        }
        return CellState.FULL;
    }

}
