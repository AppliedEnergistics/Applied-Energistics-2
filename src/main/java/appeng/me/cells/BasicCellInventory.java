/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.me.cells;

import com.google.common.base.Preconditions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackList;
import appeng.core.AEConfig;
import appeng.core.AELog;

class BasicCellInventory<T extends IAEStack> implements IMEInventory<T> {
    private static final int MAX_ITEM_TYPES = 63;
    private static final String ITEM_TYPE_TAG = "it";
    private static final String ITEM_COUNT_TAG = "ic";
    private static final String ITEM_SLOT = "#";
    private static final String[] ITEM_SLOT_KEYS = new String[MAX_ITEM_TYPES];

    static {
        for (int x = 0; x < MAX_ITEM_TYPES; x++) {
            ITEM_SLOT_KEYS[x] = ITEM_SLOT + x;
        }
    }

    private final IStorageChannel<T> channel;
    private final CompoundTag tagCompound;
    private final ISaveProvider container;
    private int maxItemTypes;
    private short storedItems;
    private long storedItemCount;
    private IAEStackList<T> cellItems;
    private final ItemStack i;
    private final IBasicCellItem<T> cellType;
    private final int itemsPerByte;
    private boolean isPersisted = true;

    private BasicCellInventory(IBasicCellItem<T> cellType, ItemStack o, ISaveProvider container) {
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
        this.storedItemCount = this.tagCompound.getLong(ITEM_COUNT_TAG);
        this.cellItems = null;
        this.channel = cellType.getChannel();
    }

    @SuppressWarnings("unchecked")
    public static <T extends IAEStack> BasicCellInventory<T> createInventory(ItemStack o, ISaveProvider container,
            IStorageChannel<T> channel) {
        Preconditions.checkNotNull(o, "Cannot create cell inventory for null itemstack");

        if (!(o.getItem() instanceof IBasicCellItem<?>cellType)) {
            return null;
        }

        if (!cellType.isStorageCell(o)) {
            // This is not an error. Items may decide to not be a storage cell temporarily.
            return null;
        }

        // The cell type's channel matches, so this cast is safe
        if (cellType.getChannel() == channel) {
            return new BasicCellInventory<>((IBasicCellItem<T>) cellType, o, container);
        } else {
            return null;
        }
    }

    public static boolean isCell(final ItemStack input) {
        return getStorageCell(input) != null;
    }

    private boolean isStorageCell(final T input) {
        if (input instanceof IAEItemStack stack) {
            final IBasicCellItem<?> type = getStorageCell(stack.getDefinition());

            return type != null && !type.storableInStorageCell();
        }

        return false;
    }

    private static IBasicCellItem<?> getStorageCell(final ItemStack input) {
        if (input != null) {
            final Item type = input.getItem();

            if (type instanceof IBasicCellItem) {
                return (IBasicCellItem<?>) type;
            }
        }

        return null;
    }

    private static boolean isCellEmpty(BasicCellInventory<?> inv) {
        if (inv != null) {
            return inv.getAvailableItems().isEmpty();
        }
        return true;
    }

    protected IAEStackList<T> getCellItems() {
        if (this.cellItems == null) {
            this.cellItems = this.getChannel().createList();
            this.loadCellItems();
        }

        return this.cellItems;
    }

    public void persist() {
        if (this.isPersisted) {
            return;
        }

        long itemCount = 0;

        // add new pretty stuff...
        int x = 0;
        for (final T v : this.cellItems) {
            itemCount += v.getStackSize();

            final CompoundTag g = new CompoundTag();
            v.writeToNBT(g);
            this.tagCompound.put(ITEM_SLOT_KEYS[x], g);

            x++;
        }

        short oldStoredItems = this.tagCompound.getShort(ITEM_TYPE_TAG);

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
            this.tagCompound.putLong(ITEM_COUNT_TAG, itemCount);
        }

        // clean any old crusty stuff...
        for (; x < oldStoredItems && x < this.maxItemTypes; x++) {
            this.tagCompound.remove(ITEM_SLOT_KEYS[x]);
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
            this.container.saveChanges();
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
            needsUpdate |= !this.loadCellItem(compoundTag);
        }

        if (needsUpdate) {
            this.saveChanges();
        }
    }

    @Override
    public IAEStackList<T> getAvailableItems(final IAEStackList<T> out) {
        for (final T item : this.getCellItems()) {
            out.add(item);
        }

        return out;
    }

    public double getIdleDrain() {
        return this.cellType.getIdleDrain();
    }

    public FuzzyMode getFuzzyMode() {
        return this.cellType.getFuzzyMode(this.i);
    }

    public InternalInventory getConfigInventory() {
        return this.cellType.getConfigInventory(this.i);
    }

    public InternalInventory getUpgradesInventory() {
        return this.cellType.getUpgradesInventory(this.i);
    }

    public int getBytesPerType() {
        return this.cellType.getBytesPerType(this.i);
    }

    public boolean canHoldNewItem() {
        final long bytesFree = this.getFreeBytes();
        return (bytesFree > this.getBytesPerType()
                || bytesFree == this.getBytesPerType() && this.getUnusedItemCount() > 0)
                && this.getRemainingItemTypes() > 0;
    }

    public long getTotalBytes() {
        return this.cellType.getBytes(this.i);
    }

    public long getFreeBytes() {
        return this.getTotalBytes() - this.getUsedBytes();
    }

    public long getTotalItemTypes() {
        return this.maxItemTypes;
    }

    public long getStoredItemCount() {
        return this.storedItemCount;
    }

    public long getStoredItemTypes() {
        return this.storedItems;
    }

    public long getRemainingItemTypes() {
        var basedOnStorage = this.getFreeBytes() / this.getBytesPerType();
        var baseOnTotal = this.getTotalItemTypes() - this.getStoredItemTypes();
        return Math.min(basedOnStorage, baseOnTotal);
    }

    public long getUsedBytes() {
        final long bytesForItemCount = (this.getStoredItemCount() + this.getUnusedItemCount()) / this.itemsPerByte;
        return this.getStoredItemTypes() * this.getBytesPerType() + bytesForItemCount;
    }

    public long getRemainingItemCount() {
        final long remaining = this.getFreeBytes() * this.itemsPerByte + this.getUnusedItemCount();
        return remaining > 0 ? remaining : 0;
    }

    public int getUnusedItemCount() {
        final int div = (int) (this.getStoredItemCount() % 8);

        if (div == 0) {
            return 0;
        }

        return this.itemsPerByte - div;
    }

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

    @Override
    public T injectItems(T input, Actionable mode, IActionSource src) {
        if (input == null) {
            return null;
        }
        if (input.getStackSize() == 0) {
            return null;
        }

        if (this.cellType.isBlackListed(this.i, input)) {
            return input;
        }
        // This is slightly hacky as it expects a read-only access, but fine for now.
        // TODO: Guarantee a read-only access. E.g. provide an isEmpty() method and
        // ensure CellInventory does not write
        // any NBT data for empty cells instead of relying on an empty IAEStackContainer
        if (this.isStorageCell(input)) {
            // TODO: make it work for any cell, and not just BasicCellInventory!
            var meInventory = createInventory(((IAEItemStack) input).createItemStack(), null, getChannel());
            if (!isCellEmpty(meInventory)) {
                return input;
            }
        }

        final T l = this.getCellItems().findPrecise(input);
        if (l != null) {
            final long remainingItemCount = this.getRemainingItemCount();
            if (remainingItemCount <= 0) {
                return input;
            }

            if (input.getStackSize() > remainingItemCount) {
                final T r = IAEStack.copy(input);
                r.setStackSize(r.getStackSize() - remainingItemCount);
                if (mode == Actionable.MODULATE) {
                    l.setStackSize(l.getStackSize() + remainingItemCount);
                    this.saveChanges();
                }
                return r;
            } else {
                if (mode == Actionable.MODULATE) {
                    l.setStackSize(l.getStackSize() + input.getStackSize());
                    this.saveChanges();
                }
                return null;
            }
        }

        if (this.canHoldNewItem()) // room for new type, and for at least one item!
        {
            long remainingItemCount = this.getRemainingItemCount()
                    - (long) this.getBytesPerType() * this.itemsPerByte;
            if (remainingItemCount > 0) {
                if (input.getStackSize() > remainingItemCount) {
                    final T toReturn = IAEStack.copy(input);
                    toReturn.setStackSize(input.getStackSize() - remainingItemCount);
                    if (mode == Actionable.MODULATE) {
                        final T toWrite = IAEStack.copy(input);
                        toWrite.setStackSize(remainingItemCount);

                        this.cellItems.add(toWrite);
                        this.saveChanges();
                    }
                    return toReturn;
                }

                if (mode == Actionable.MODULATE) {
                    this.cellItems.add(input);
                    this.saveChanges();
                }

                return null;
            }
        }

        return input;
    }

    @Override
    public T extractItems(T request, Actionable mode, IActionSource src) {
        if (request == null) {
            return null;
        }

        final long size = Math.min(Integer.MAX_VALUE, request.getStackSize());

        T Results = null;

        final T l = this.getCellItems().findPrecise(request);
        if (l != null) {
            Results = IAEStack.copy(l);

            if (l.getStackSize() <= size) {
                Results.setStackSize(l.getStackSize());
                if (mode == Actionable.MODULATE) {
                    l.setStackSize(0);
                    this.saveChanges();
                }
            } else {
                Results.setStackSize(size);
                if (mode == Actionable.MODULATE) {
                    l.setStackSize(l.getStackSize() - size);
                    this.saveChanges();
                }
            }
        }

        return Results;
    }

    @Override
    public IStorageChannel<T> getChannel() {
        return this.channel;
    }

    /**
     * Load a single item.
     *
     * @return true when successfully loaded
     */
    private boolean loadCellItem(CompoundTag compoundTag) {
        // Now load the item stack
        T t;
        try {
            t = this.getChannel().createFromNBT(compoundTag);
            if (t == null) {
                AELog.warn("Removing item " + compoundTag
                        + " from storage cell because the associated item type couldn't be found.");
                return false;
            }
        } catch (Throwable ex) {
            if (AEConfig.instance().isRemoveCrashingItemsOnLoad()) {
                AELog.warn(ex,
                        "Removing item " + compoundTag + " from storage cell because loading the ItemStack crashed.");
                return false;
            }
            throw ex;
        }

        if (t.getStackSize() > 0) {
            this.cellItems.add(t);
        }

        return true;
    }
}
