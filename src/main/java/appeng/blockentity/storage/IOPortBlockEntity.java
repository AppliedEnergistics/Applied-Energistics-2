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

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
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
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageCells;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackList;
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
import appeng.util.Platform;
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
    private ItemStack currentCell;
    private Map<IStorageChannel<?>, IMEInventory<?>> cachedInventories;

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
    public CompoundTag save(final CompoundTag data) {
        super.save(data);
        this.manager.writeToNBT(data);
        this.upgrades.writeToNBT(data, "upgrades");
        data.putInt("lastRedstoneState", this.lastRedstoneState.ordinal());
        return data;
    }

    @Override
    public void load(final CompoundTag data) {
        super.load(data);
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

    @Nonnull
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
    public void onChangeInventory(final InternalInventory inv, final int slot,
            final ItemStack removed, final ItemStack added) {
        if (this.inputCells == inv) {
            this.updateTask();
        }
    }

    @Override
    public InternalInventory getExposedInventoryForSide(final Direction facing) {
        if (facing == this.getUp() || facing == this.getUp().getOpposite()) {
            return this.inputCellsExt;
        } else {
            return this.outputCellsExt;
        }
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(TickRates.IOPort.getMin(), TickRates.IOPort.getMax(), !this.hasWork(), false);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
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

        var energy = grid.getEnergyService();
        for (int x = 0; x < NUMBER_OF_CELL_SLOTS; x++) {
            final ItemStack is = this.inputCells.getStackInSlot(x);
            if (!is.isEmpty()) {
                boolean shouldMove = true;

                for (var c : StorageChannels.getAll()) {
                    if (itemsToMove > 0) {
                        final IMEMonitor<? extends IAEStack> network = grid.getStorageService()
                                .getInventory(c);
                        final IMEInventory<?> inv = this.getInv(is, c);

                        if (inv == null) {
                            continue;
                        }

                        if (this.manager.getSetting(Settings.OPERATION_MODE) == OperationMode.EMPTY) {
                            itemsToMove = this.transferContents(energy, inv, network, itemsToMove, c);
                        } else {
                            itemsToMove = this.transferContents(energy, network, inv, itemsToMove, c);
                        }

                        shouldMove &= this.shouldMove(inv);

                        if (itemsToMove > 0) {
                            ret = TickRateModulation.IDLE;
                        } else {
                            ret = TickRateModulation.URGENT;
                        }
                    }
                }

                if (itemsToMove > 0 && shouldMove && this.moveSlot(x)) {
                }
                ret = TickRateModulation.URGENT;

            }
        }

        return ret;
    }

    private IMEInventory<?> getInv(final ItemStack is, final IStorageChannel<?> chan) {
        if (this.currentCell != is) {
            this.currentCell = is;
            this.cachedInventories = new IdentityHashMap<>();

            for (var c : StorageChannels.getAll()) {
                this.cachedInventories.put(c, StorageCells.getCellInventory(is, null, c));
            }
        }

        return this.cachedInventories.get(chan);
    }

    private long transferContents(final IEnergySource energy, final IMEInventory src, final IMEInventory destination,
            long itemsToMove, final IStorageChannel chan) {
        final IAEStackList<? extends IAEStack> myList;
        if (src instanceof IMEMonitor) {
            myList = ((IMEMonitor) src).getStorageList();
        } else {
            myList = src.getAvailableItems();
        }

        itemsToMove *= chan.transferFactor();

        boolean didStuff;

        do {
            didStuff = false;

            for (final IAEStack s : myList) {
                final long totalStackSize = s.getStackSize();
                if (totalStackSize > 0) {
                    final IAEStack stack = destination.injectItems(s, Actionable.SIMULATE, this.mySrc);

                    long possible = 0;
                    if (stack == null) {
                        possible = totalStackSize;
                    } else {
                        possible = totalStackSize - stack.getStackSize();
                    }

                    if (possible > 0) {
                        possible = Math.min(possible, itemsToMove);
                        s.setStackSize(possible);

                        final IAEStack extracted = src.extractItems(s, Actionable.MODULATE, this.mySrc);
                        if (extracted != null) {
                            possible = extracted.getStackSize();
                            final IAEStack failed = Platform.poweredInsert(energy, destination, extracted, this.mySrc);

                            if (failed != null) {
                                possible -= failed.getStackSize();
                                src.injectItems(failed, Actionable.MODULATE, this.mySrc);
                            }

                            if (possible > 0) {
                                itemsToMove -= possible;
                                didStuff = true;
                            }

                            break;
                        }
                    }
                }
            }
        } while (itemsToMove > 0 && didStuff);

        return itemsToMove / chan.transferFactor();
    }

    private boolean shouldMove(final IMEInventory<?> inv) {
        final FullnessMode fm = this.manager.getSetting(Settings.FULLNESS_MODE);

        if (inv != null) {
            return this.matches(fm, inv);
        }

        return true;
    }

    private boolean moveSlot(final int x) {
        if (this.outputCells.addItems(this.inputCells.getStackInSlot(x)).isEmpty()) {
            this.inputCells.setItemDirect(x, ItemStack.EMPTY);
            return true;
        }
        return false;
    }

    private boolean matches(final FullnessMode fm, final IMEInventory src) {
        if (fm == FullnessMode.HALF) {
            return true;
        }

        final IAEStackList<? extends IAEStack> myList;

        if (src instanceof IMEMonitor) {
            myList = ((IMEMonitor) src).getStorageList();
        } else {
            myList = src.getAvailableItems();
        }

        if (fm == FullnessMode.EMPTY) {
            return myList.isEmpty();
        }

        final IAEStack test = myList.getFirstItem();
        if (test != null) {
            test.setStackSize(1);
            return src.injectItems(test, Actionable.SIMULATE, this.mySrc) != null;
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
    public void getDrops(final Level level, final BlockPos pos, final List<ItemStack> drops) {
        super.getDrops(level, pos, drops);

        for (var upgrade : upgrades) {
            drops.add(upgrade);
        }
    }
}
