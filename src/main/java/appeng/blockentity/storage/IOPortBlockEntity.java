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

package appeng.blockentity.storage;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.ints.AbstractInt2ObjectMap;

import appeng.api.config.Actionable;
import appeng.api.config.FullnessMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.OperationMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageCells;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.StorageCell;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.grid.AENetworkedInvBlockEntity;
import appeng.core.AELog;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.me.cells.BasicCellInventory;
import appeng.me.helpers.MachineSource;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.AEItemFilters;

public class IOPortBlockEntity extends AENetworkedInvBlockEntity
        implements IUpgradeableObject, IConfigurableObject, IGridTickable {
    private static final int NUMBER_OF_CELL_SLOTS = 6;
    private static final int NUMBER_OF_UPGRADE_SLOTS = 3;
    private static final int TRANSFER_TYPES_PER_TICK = 63 * NUMBER_OF_CELL_SLOTS;

    private final IConfigManager manager;

    private final AppEngInternalInventory inputCells = new AppEngInternalInventory(this, NUMBER_OF_CELL_SLOTS);
    private final AppEngInternalInventory outputCells = new AppEngInternalInventory(this, NUMBER_OF_CELL_SLOTS);
    private final InternalInventory combinedInventory = new CombinedInternalInventory(this.inputCells,
            this.outputCells);

    private final InternalInventory inputCellsExt = new FilteredInternalInventory(this.inputCells,
            AEItemFilters.INSERT_ONLY);
    private final InternalInventory outputCellsExt = new FilteredInternalInventory(this.outputCells,
            AEItemFilters.EXTRACT_ONLY);

    private final IUpgradeInventory upgrades;
    private final IActionSource mySrc;
    private YesNo lastRedstoneState;

    private boolean isActive = false;

    public IOPortBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(IGridTickable.class, this);
        this.manager = IConfigManager.builder(this::updateTask)
                .registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE)
                .registerSetting(Settings.FULLNESS_MODE, FullnessMode.EMPTY)
                .registerSetting(Settings.OPERATION_MODE, OperationMode.EMPTY)
                .build();
        this.mySrc = new MachineSource(this);
        this.lastRedstoneState = YesNo.UNDECIDED;

        this.upgrades = UpgradeInventories.forMachine(AEBlocks.IO_PORT, NUMBER_OF_UPGRADE_SLOTS, this::saveChanges);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        this.manager.writeToNBT(data, registries);
        this.upgrades.writeToNBT(data, "upgrades", registries);
        data.putInt("lastRedstoneState", this.lastRedstoneState.ordinal());
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.manager.readFromNBT(data, registries);
        this.upgrades.readFromNBT(data, "upgrades", registries);
        if (data.contains("lastRedstoneState")) {
            this.lastRedstoneState = YesNo.values()[data.getInt("lastRedstoneState")];
        }
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(this.isActive());
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean ret = super.readFromStream(data);

        final boolean isActive = data.readBoolean();
        ret = isActive != this.isActive || ret;
        this.isActive = isActive;

        return ret;
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    private void updateTask() {
        getMainNode().ifPresent((grid, node) -> {
            if (this.hasWork()) {
                grid.getTickManager().wakeDevice(node);
            } else {
                grid.getTickManager().sleepDevice(node);
            }
        });
    }

    public void updateRedstoneState() {
        if (this.level != null) {
            final YesNo currentState = this.level.getBestNeighborSignal(this.worldPosition) != 0 ? YesNo.YES : YesNo.NO;
            if (this.lastRedstoneState != currentState) {
                this.lastRedstoneState = currentState;
                this.updateTask();
            }
        }

    }

    private boolean getRedstoneState() {
        if (this.lastRedstoneState == YesNo.UNDECIDED) {
            this.updateRedstoneState();
        }

        return this.lastRedstoneState == YesNo.YES;
    }

    private boolean isEnabled() {
        if (!upgrades.isInstalled(AEItems.REDSTONE_CARD)) {
            return true;
        }

        final RedstoneMode rs = this.manager.getSetting(Settings.REDSTONE_CONTROLLED);
        if (rs == RedstoneMode.HIGH_SIGNAL) {
            return this.getRedstoneState();
        }
        return !this.getRedstoneState();
    }

    public boolean isActive() {
        if (level != null && !level.isClientSide) {
            return this.getMainNode().isOnline();
        } else {
            return this.isActive;
        }
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (reason != IGridNodeListener.State.GRID_BOOT) {
            this.markForUpdate();
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.manager;
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return this.upgrades;
    }

    @Nullable
    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(ISegmentedInventory.UPGRADES)) {
            return this.upgrades;
        } else if (id.equals(ISegmentedInventory.CELLS)) {
            return this.combinedInventory;
        } else {
            return super.getSubInventory(id);
        }
    }

    private boolean hasWork() {
        if (this.isEnabled()) {

            return !this.inputCells.isEmpty();
        }

        return false;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.combinedInventory;
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        if (this.inputCells == inv) {
            this.updateTask();
        }
    }

    @Override
    protected InternalInventory getExposedInventoryForSide(Direction facing) {
        if (facing == this.getTop() || facing == this.getTop().getOpposite()) {
            return this.inputCellsExt;
        } else {
            return this.outputCellsExt;
        }
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.IOPort, !this.hasWork());
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!this.getMainNode().isActive()) {
            return TickRateModulation.IDLE;
        }

        TickRateModulation ret = TickRateModulation.SLEEP;
        long itemsToMove = 256;

        switch (upgrades.getInstalledUpgrades(AEItems.SPEED_CARD)) {
            case 1 -> itemsToMove *= 2;
            case 2 -> itemsToMove *= 4;
            case 3 -> itemsToMove *= 8;
        }

        var grid = getMainNode().getGrid();
        if (grid == null) {
            return TickRateModulation.IDLE;
        }

        if (this.manager.getSetting(Settings.OPERATION_MODE) == OperationMode.EMPTY) {
            ret = emptyCells(grid, itemsToMove, ret);
        } else {
            ret = fillCells(grid, itemsToMove, ret);
        }

        return ret;
    }

    /**
     * Work is complete when the inventory has reached the desired end-state.
     */
    public boolean matchesFullnessMode(StorageCell inv) {
        var fullnessMode = manager.getSetting(Settings.FULLNESS_MODE);
        return matchesFullnessMode(inv, fullnessMode);
    }

    protected static boolean matchesFullnessMode(StorageCell inv, FullnessMode fullnessMode) {
        return switch (fullnessMode) {
            // In this mode, work completes as soon as no more items are moved within one operation,
            // independent of the actual inventory state
            case HALF -> true;
            case EMPTY -> inv.getStatus() == CellState.EMPTY;
            case FULL -> inv.getStatus() == CellState.FULL;
        };
    }

    private static long transferEntryToCell(AEKey key, MEStorage src, StorageCell destination, long itemsToMove,
            IEnergySource energy, IActionSource actionSource) {
        long movedItem = 0;
        if (itemsToMove > 0) {
            var possible = destination.insert(key, itemsToMove, Actionable.SIMULATE, actionSource);
            if (possible > 0) {
                possible = Math.min(possible, itemsToMove * key.getAmountPerOperation());

                possible = src.extract(key, possible, Actionable.MODULATE, actionSource);
                if (possible > 0) {
                    var inserted = StorageHelper.poweredInsert(energy, destination, key, possible, actionSource);

                    if (inserted < possible) {
                        src.insert(key, possible - inserted, Actionable.MODULATE, actionSource);
                    }

                    if (inserted > 0) {
                        movedItem = Math.max(1, inserted / key.getAmountPerOperation());
                    }
                }
            }
        }
        return movedItem;
    }

    private static long transferEntryFromCell(AEKey key, StorageCell src, MEStorage destination, long itemsToMove,
            IEnergySource energy, IActionSource actionSource) {
        long movedItem = 0;
        if (itemsToMove > 0) {
            // itemsToMove must be the number StorageCell just reported or less, so we can first modulate the
            // NetworkStorage
            var inserted = StorageHelper.poweredInsert(energy, destination, key, itemsToMove, actionSource,
                    Actionable.MODULATE);
            if (inserted > 0) {
                movedItem = Math.max(1, inserted / key.getAmountPerOperation());
                var extracted = src.extract(key, inserted, Actionable.MODULATE, actionSource);
                long mismatch = inserted - extracted;
                if (mismatch > 0) {
                    AELog.warn("%s lied! Extracting some items back from NetworkStorage to prevent item duplication!",
                            src.toString());
                    inserted -= destination.extract(key, mismatch, Actionable.MODULATE, actionSource);
                    movedItem = -Math.max(1, inserted / key.getAmountPerOperation());
                } else if (mismatch < 0) {
                    AELog.warn(
                            "%s extracted too much items! Inserting some items back to StorageCell to prevent item loss!",
                            src.toString());
                    var remaining = -mismatch;
                    extracted -= src.insert(key, remaining, Actionable.MODULATE, actionSource);
                    movedItem = -movedItem;
                }
                if (extracted != inserted)
                    throw new ConcurrentModificationException(
                            "Failed to recover the mismatch of extraction and insertion!");
            }
        }
        return movedItem;
    }

    private TickRateModulation fillCells(IGrid grid, long itemsToMove, TickRateModulation tickRateModulation) {
        var networkInv = grid.getStorageService().getInventory();
        KeyCounter srcList = grid.getStorageService().getCachedInventory();
        KeyCounter dstList = null;
        int movedTypes = 0;
        boolean isUrgent = false;
        var energy = grid.getEnergyService();
        // The fastest way to manage <65 flags is to use primitive integer types
        long cellFlags = 0;
        Objects.checkIndex(NUMBER_OF_CELL_SLOTS, Long.SIZE);
        ArrayList<AbstractInt2ObjectMap.BasicEntry<StorageCell>> typeFilledCells = new ArrayList<>();
        ArrayList<AbstractInt2ObjectMap.BasicEntry<BasicCellInventory>> whitelistedCells = new ArrayList<>();
        ArrayList<AbstractInt2ObjectMap.BasicEntry<StorageCell>> cells = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_CELL_SLOTS; i++) {
            var cell = this.inputCells.getStackInSlot(i);
            if (cell == null)
                continue;
            var cellInv = StorageCells.getCellInventory(cell, null);

            if (cellInv == null) {
                // This item is not a valid storage cell, try to move it to the output
                moveSlot(i);
                continue;
            }
            var status = cellInv.getStatus();
            switch (status) {
                case FULL -> {
                    // This item is not a valid storage cell, try to move it to the output
                    moveSlot(i);
                    continue;
                }
                case TYPES_FULL -> {
                    typeFilledCells.add(new AbstractInt2ObjectMap.BasicEntry<>(i, cellInv));
                    continue;
                }
            }
            if (cellInv instanceof BasicCellInventory basicCellInventory) {
                if (basicCellInventory.getFreeBytes() <= 0) {
                    moveSlot(i);
                    continue;
                }
                if (basicCellInventory.isPreformatted()
                        && basicCellInventory.getPartitionListMode() == IncludeExclude.WHITELIST) {
                    whitelistedCells.add(new AbstractInt2ObjectMap.BasicEntry<>(i, basicCellInventory));
                    continue;
                }
            }
            cells.add(new AbstractInt2ObjectMap.BasicEntry<>(i, cellInv));
        }
        if (cells.isEmpty() && whitelistedCells.isEmpty() && typeFilledCells.isEmpty())
            return tickRateModulation;
        if (!whitelistedCells.isEmpty()) {
            dstList = new KeyCounter();
            int i = 0;
            while (itemsToMove > 0 && movedTypes < TRANSFER_TYPES_PER_TICK && i < whitelistedCells.size()) {
                var cell = whitelistedCells.get(i);
                var slot = cell.getIntKey();
                var cellInv = cell.getValue();
                var limitToAdd = cellInv instanceof BasicCellInventory basicCellInventory
                        ? basicCellInventory.getRemainingItemTypes()
                        : 0;
                limitToAdd = Math.max(63, limitToAdd);
                if (cellInv.filterMatches(srcList, dstList, movedTypes + limitToAdd)) {
                    for (var entry : dstList) {
                        var what = entry.getKey();
                        var totalStackSize = entry.getLongValue();
                        if (totalStackSize > 0) {
                            var movable = Math.min(totalStackSize, itemsToMove);
                            var cellMovedItems = transferEntryToCell(what, networkInv, cellInv, movable, energy,
                                    this.mySrc);
                            itemsToMove -= cellMovedItems;
                            movedTypes += cellMovedItems > 0 ? 1 : 0;
                            if (cellMovedItems > 0) {
                                cellFlags |= 1L << slot;
                                var status = cellInv.getStatus();
                                if (status == CellState.FULL) {
                                    isUrgent |= this.moveSlot(cell.getIntKey());
                                    whitelistedCells.remove(i);
                                    break;
                                }
                            }
                            if (itemsToMove <= 0 || movedTypes >= TRANSFER_TYPES_PER_TICK)
                                break;
                        }
                    }
                    dstList.clear();
                    i++;
                } else {
                    cells.add(new AbstractInt2ObjectMap.BasicEntry<>(slot, cellInv));
                    whitelistedCells.remove(i);
                }
            }
        }
        var cellsChecked = itemsToMove > 0 && movedTypes < TRANSFER_TYPES_PER_TICK;
        if (!cells.isEmpty()) {
            // This avoids checking the same key multiple times
            var srcIterator = srcList.iterator();
            while (srcIterator.hasNext() && itemsToMove > 0 && movedTypes < TRANSFER_TYPES_PER_TICK
                    && !cells.isEmpty()) {
                var srcEntry = srcIterator.next();
                var totalStackSize = srcEntry.getLongValue();
                if (totalStackSize > 0) {
                    var what = srcEntry.getKey();
                    var movable = Math.min(totalStackSize, itemsToMove);
                    long movedItems = 0;
                    int x = 0;
                    do {
                        var cell = cells.get(x);
                        var cellInv = cell.getValue();
                        var cellMovedItems = transferEntryToCell(what, networkInv, cellInv, movable, energy,
                                this.mySrc);
                        movable -= cellMovedItems;
                        movedItems += cellMovedItems;
                        if (cellMovedItems > 0) {
                            int slot = cell.getIntKey();
                            cellFlags |= 1L << slot;
                            var status = cellInv.getStatus();
                            if (status == CellState.FULL) {
                                isUrgent |= this.moveSlot(slot);
                                cells.remove(x);
                                continue;
                            } else if (status == CellState.TYPES_FULL) {
                                // Postpone to the next tick
                                cells.remove(x);
                                continue;
                            }
                        }
                        x++;
                    } while (movable > 0 && x < cells.size());
                    itemsToMove -= movedItems;
                    movedTypes += movedItems > 0 ? 1 : 0;
                }
            }
        }
        var typeFilledCellsChecked = itemsToMove > 0 && movedTypes < TRANSFER_TYPES_PER_TICK;
        if (!typeFilledCells.isEmpty()) {
            if (dstList == null)
                dstList = new KeyCounter();
            else
                dstList.clear();
            var filterList = new KeyCounter();
            int i = 0;
            while (itemsToMove > 0 && movedTypes < TRANSFER_TYPES_PER_TICK && i < typeFilledCells.size()) {
                var cell = typeFilledCells.get(i);
                var slot = cell.getIntKey();
                var cellInv = cell.getValue();
                boolean cellRemoved = false;
                cellInv.getAvailableStacks(filterList);
                if (filterList.filterMatchesPrecise(srcList, dstList)) {
                    if (!dstList.isEmpty()) {
                        for (var entry : dstList) {
                            var what = entry.getKey();
                            var totalStackSize = entry.getLongValue();
                            if (totalStackSize > 0) {
                                var movable = Math.min(totalStackSize, itemsToMove);
                                var cellMovedItems = transferEntryToCell(what, networkInv, cellInv, movable, energy,
                                        this.mySrc);
                                itemsToMove -= cellMovedItems;
                                movedTypes += cellMovedItems > 0 ? 1 : 0;
                                if (cellMovedItems > 0) {
                                    cellFlags |= 1L << slot;
                                    var status = cellInv.getStatus();
                                    if (status == CellState.FULL) {
                                        isUrgent |= this.moveSlot(slot);
                                        typeFilledCells.remove(i);
                                        cellRemoved = true;
                                        break;
                                    }
                                }
                                if (itemsToMove <= 0 || movedTypes >= TRANSFER_TYPES_PER_TICK)
                                    break;
                            }
                        }
                        dstList.clear();
                    } else {
                        isUrgent |= this.moveSlot(slot);
                        typeFilledCells.remove(i);
                    }
                }
                i += cellRemoved ? 0 : 1;
            }
        }
        isUrgent |= itemsToMove <= 0;
        var fullnessMode = manager.getSetting(Settings.FULLNESS_MODE);
        var checkFullnessMode = itemsToMove > 0 && movedTypes < TRANSFER_TYPES_PER_TICK;
        if (fullnessMode != FullnessMode.FULL) {
            if (!whitelistedCells.isEmpty()) {
                for (var cell : whitelistedCells) {
                    int slot = cell.getIntKey();
                    var p = 1L << slot;
                    if ((cellFlags & p) == 0
                            || (checkFullnessMode && matchesFullnessMode(cell.getValue(), fullnessMode))) {
                        isUrgent |= this.moveSlot(slot);
                    }
                    cellFlags &= ~p;
                }
            }
            if (cellsChecked && !cells.isEmpty()) {
                for (var cell : cells) {
                    int slot = cell.getIntKey();
                    var p = 1L << slot;
                    if ((cellFlags & p) == 0
                            || (checkFullnessMode && matchesFullnessMode(cell.getValue(), fullnessMode))) {
                        isUrgent |= this.moveSlot(slot);
                    }
                    cellFlags &= ~p;
                }
            }
            if (typeFilledCellsChecked && !typeFilledCells.isEmpty()) {
                for (var cell : typeFilledCells) {
                    int slot = cell.getIntKey();
                    var p = 1L << slot;
                    if ((cellFlags & p) == 0
                            || (checkFullnessMode && matchesFullnessMode(cell.getValue(), fullnessMode))) {
                        isUrgent |= this.moveSlot(slot);
                    }
                    cellFlags &= ~p;
                }
            }
        }
        return isUrgent ? TickRateModulation.URGENT : TickRateModulation.IDLE;
    }

    private TickRateModulation emptyCells(IGrid grid, long itemsToMove, TickRateModulation tickRateModulation) {
        var networkInv = grid.getStorageService().getInventory();

        KeyCounter srcList = new KeyCounter();
        int x = 0;
        int movedTypes = 0;

        var energy = grid.getEnergyService();
        do {
            var cell = this.inputCells.getStackInSlot(x);
            var cellInv = StorageCells.getCellInventory(cell, null);
            if (cellInv == null) {
                // This item is not a valid storage cell, try to move it to the output
                moveSlot(x);
                continue;
            }
            srcList.clear();
            cellInv.getAvailableStacks(srcList);
            var srcIterator = srcList.iterator();
            while (srcIterator.hasNext() && itemsToMove > 0 && movedTypes < TRANSFER_TYPES_PER_TICK) {
                var srcEntry = srcIterator.next();
                var totalStackSize = srcEntry.getLongValue();
                if (totalStackSize > 0) {
                    var what = srcEntry.getKey();
                    long movedItems = transferEntryFromCell(what, cellInv, networkInv,
                            Math.min(totalStackSize, itemsToMove), energy, this.mySrc);
                    itemsToMove -= Math.abs(movedItems);
                    if (movedItems < 0) {
                        // Apparently the cell lied about its stored amount.
                        return TickRateModulation.SLOWER;
                    }
                    movedTypes += movedItems > 0 ? 1 : 0;
                }
            }
            if (itemsToMove <= 0 || (matchesFullnessMode(cellInv) && this.moveSlot(x))) {
                tickRateModulation = TickRateModulation.URGENT;
            } else {
                tickRateModulation = TickRateModulation.IDLE;
            }
        } while (itemsToMove > 0 && movedTypes < TRANSFER_TYPES_PER_TICK && ++x < NUMBER_OF_CELL_SLOTS);

        return tickRateModulation;
    }

    private boolean moveSlot(int x) {
        if (this.outputCells.addItems(this.inputCells.getStackInSlot(x)).isEmpty()) {
            this.inputCells.setItemDirect(x, ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    /**
     * Adds the items in the upgrade slots to the drop list.
     *
     * @param level level
     * @param pos   pos of block entity
     * @param drops drops of block entity
     */
    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);

        for (var upgrade : upgrades) {
            drops.add(upgrade);
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        upgrades.clear();
    }
}
