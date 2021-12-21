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

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.config.Actionable;
import appeng.api.config.FullnessMode;
import appeng.api.config.OperationMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.implementations.IUpgradeInventory;
import appeng.api.implementations.IUpgradeableObject;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageCells;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.StorageCell;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.settings.TickRates;
import appeng.me.helpers.MachineSource;
import appeng.parts.automation.BlockUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.util.ConfigManager;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.AEItemFilters;

public class IOPortBlockEntity extends AENetworkInvBlockEntity
        implements IUpgradeableObject, IConfigurableObject, IGridTickable {
    private static final int NUMBER_OF_CELL_SLOTS = 6;
    private static final int NUMBER_OF_UPGRADE_SLOTS = 3;

    private final ConfigManager manager;

    private final AppEngInternalInventory inputCells = new AppEngInternalInventory(this, NUMBER_OF_CELL_SLOTS);
    private final AppEngInternalInventory outputCells = new AppEngInternalInventory(this, NUMBER_OF_CELL_SLOTS);
    private final InternalInventory combinedInventory = new CombinedInternalInventory(this.inputCells,
            this.outputCells);

    private final InternalInventory inputCellsExt = new FilteredInternalInventory(this.inputCells,
            AEItemFilters.INSERT_ONLY);
    private final InternalInventory outputCellsExt = new FilteredInternalInventory(this.outputCells,
            AEItemFilters.EXTRACT_ONLY);

    private final UpgradeInventory upgrades;
    private final IActionSource mySrc;
    private YesNo lastRedstoneState;

    public IOPortBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(IGridTickable.class, this);
        this.manager = new ConfigManager((manager, setting) -> this.updateTask());
        this.manager.registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        this.manager.registerSetting(Settings.FULLNESS_MODE, FullnessMode.EMPTY);
        this.manager.registerSetting(Settings.OPERATION_MODE, OperationMode.EMPTY);
        this.mySrc = new MachineSource(this);
        this.lastRedstoneState = YesNo.UNDECIDED;

        final Block ioPortBlock = AEBlocks.IO_PORT.block();
        this.upgrades = new BlockUpgradeInventory(ioPortBlock, this, NUMBER_OF_UPGRADE_SLOTS);
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        this.manager.writeToNBT(data);
        this.upgrades.writeToNBT(data, "upgrades");
        data.putInt("lastRedstoneState", this.lastRedstoneState.ordinal());
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        this.manager.readFromNBT(data);
        this.upgrades.readFromNBT(data, "upgrades");
        if (data.contains("lastRedstoneState")) {
            this.lastRedstoneState = YesNo.values()[data.getInt("lastRedstoneState")];
        }
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
        final YesNo currentState = this.level.getBestNeighborSignal(this.worldPosition) != 0 ? YesNo.YES : YesNo.NO;
        if (this.lastRedstoneState != currentState) {
            this.lastRedstoneState = currentState;
            this.updateTask();
        }
    }

    private boolean getRedstoneState() {
        if (this.lastRedstoneState == YesNo.UNDECIDED) {
            this.updateRedstoneState();
        }

        return this.lastRedstoneState == YesNo.YES;
    }

    private boolean isEnabled() {
        if (upgrades.getInstalledUpgrades(Upgrades.REDSTONE) == 0) {
            return true;
        }

        final RedstoneMode rs = this.manager.getSetting(Settings.REDSTONE_CONTROLLED);
        if (rs == RedstoneMode.HIGH_SIGNAL) {
            return this.getRedstoneState();
        }
        return !this.getRedstoneState();
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
    public void onChangeInventory(InternalInventory inv, int slot,
            ItemStack removed, ItemStack added) {
        if (this.inputCells == inv) {
            this.updateTask();
        }
    }

    @Override
    public InternalInventory getExposedInventoryForSide(Direction facing) {
        if (facing == this.getUp() || facing == this.getUp().getOpposite()) {
            return this.inputCellsExt;
        } else {
            return this.outputCellsExt;
        }
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.IOPort, !this.hasWork(), false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!this.getMainNode().isActive()) {
            return TickRateModulation.IDLE;
        }

        TickRateModulation ret = TickRateModulation.SLEEP;
        long itemsToMove = 256;

        switch (upgrades.getInstalledUpgrades(Upgrades.SPEED)) {
            case 1 -> itemsToMove *= 2;
            case 2 -> itemsToMove *= 4;
            case 3 -> itemsToMove *= 8;
        }

        var grid = getMainNode().getGrid();
        if (grid == null) {
            return TickRateModulation.IDLE;
        }

        for (int x = 0; x < NUMBER_OF_CELL_SLOTS; x++) {
            var cell = this.inputCells.getStackInSlot(x);

            var cellInv = StorageCells.getCellInventory(cell, null);

            if (cellInv == null) {
                // This item is not a valid storage cell, try to move it to the output
                moveSlot(x);
                continue;
            }

            if (itemsToMove > 0) {
                itemsToMove = transferContents(grid, cellInv, itemsToMove);

                if (itemsToMove > 0) {
                    ret = TickRateModulation.IDLE;
                } else {
                    ret = TickRateModulation.URGENT;
                }
            }

            if (itemsToMove > 0 && matchesFullnessMode(cellInv) && this.moveSlot(x)) {
                ret = TickRateModulation.URGENT;
            }
        }

        return ret;
    }

    /**
     * Work is complete when the inventory has reached the desired end-state.
     */
    public boolean matchesFullnessMode(StorageCell inv) {
        return switch (manager.getSetting(Settings.FULLNESS_MODE)) {
            // In this mode, work completes as soon as no more items are moved within one operation,
            // independent of the actual inventory state
            case HALF -> true;
            case EMPTY -> inv.getStatus() == CellState.EMPTY;
            case FULL -> inv.getStatus() == CellState.FULL;
        };
    }

    private long transferContents(IGrid grid, StorageCell cellInv, long itemsToMove) {

        var networkInv = grid.getStorageService().getInventory();

        KeyCounter srcList;
        MEStorage src, destination;
        if (this.manager.getSetting(Settings.OPERATION_MODE) == OperationMode.EMPTY) {
            src = cellInv;
            srcList = cellInv.getAvailableStacks();
            destination = networkInv;
        } else {
            src = networkInv;
            srcList = grid.getStorageService().getCachedInventory();
            destination = cellInv;
        }

        var energy = grid.getEnergyService();
        boolean didStuff;

        do {
            didStuff = false;

            for (var srcEntry : srcList) {
                var totalStackSize = srcEntry.getLongValue();
                if (totalStackSize > 0) {
                    var what = srcEntry.getKey();
                    var possible = destination.insert(what, totalStackSize, Actionable.SIMULATE, this.mySrc);

                    if (possible > 0) {
                        possible = Math.min(possible, itemsToMove * what.getAmountPerOperation());

                        possible = src.extract(what, possible, Actionable.MODULATE, this.mySrc);
                        if (possible > 0) {
                            var inserted = StorageHelper.poweredInsert(energy, destination, what, possible, this.mySrc);

                            if (inserted < possible) {
                                src.insert(what, possible - inserted, Actionable.MODULATE, this.mySrc);
                            }

                            if (inserted > 0) {
                                itemsToMove -= Math.max(1, inserted / what.getAmountPerOperation());
                                didStuff = true;
                            }

                            break;
                        }
                    }
                }
            }
        } while (itemsToMove > 0 && didStuff);

        return itemsToMove;
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
    public void getDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.getDrops(level, pos, drops);

        for (var upgrade : upgrades) {
            drops.add(upgrade);
        }
    }
}
