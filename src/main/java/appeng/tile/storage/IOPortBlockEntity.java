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

package appeng.tile.storage;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;

import appeng.api.config.Actionable;
import appeng.api.config.FullnessMode;
import appeng.api.config.OperationMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.implementations.IUpgradeableHost;
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
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.core.Api;
import appeng.core.settings.TickRates;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.parts.automation.BlockUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.grid.AENetworkInvBlockEntity;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.AdaptorFixedInv;
import appeng.util.inv.InvOperation;
import appeng.util.inv.WrapperChainedItemHandler;
import appeng.util.inv.WrapperFilteredItemHandler;
import appeng.util.inv.filter.AEItemFilters;

public class IOPortBlockEntity extends AENetworkInvBlockEntity
        implements IUpgradeableHost, IConfigManagerHost, IGridTickable {
    private static final int NUMBER_OF_CELL_SLOTS = 6;
    private static final int NUMBER_OF_UPGRADE_SLOTS = 3;

    private final ConfigManager manager;

    private final AppEngInternalInventory inputCells = new AppEngInternalInventory(this, NUMBER_OF_CELL_SLOTS);
    private final AppEngInternalInventory outputCells = new AppEngInternalInventory(this, NUMBER_OF_CELL_SLOTS);
    private final FixedItemInv combinedInventory = new WrapperChainedItemHandler(this.inputCells, this.outputCells);

    private final FixedItemInv inputCellsExt = new WrapperFilteredItemHandler(this.inputCells,
            AEItemFilters.INSERT_ONLY);
    private final FixedItemInv outputCellsExt = new WrapperFilteredItemHandler(this.outputCells,
            AEItemFilters.EXTRACT_ONLY);

    private final UpgradeInventory upgrades;
    private final IActionSource mySrc;
    private YesNo lastRedstoneState;
    private ItemStack currentCell;
    private Map<IStorageChannel<?>, IMEInventory<?>> cachedInventories;

    public IOPortBlockEntity(BlockEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        this.manager = new ConfigManager(this);
        this.manager.registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        this.manager.registerSetting(Settings.FULLNESS_MODE, FullnessMode.EMPTY);
        this.manager.registerSetting(Settings.OPERATION_MODE, OperationMode.EMPTY);
        this.mySrc = new MachineSource(this);
        this.lastRedstoneState = YesNo.UNDECIDED;

        final Block ioPortBlock = Api.instance().definitions().blocks().iOPort().maybeBlock().get();
        this.upgrades = new BlockUpgradeInventory(ioPortBlock, this, NUMBER_OF_UPGRADE_SLOTS);
    }

    @Override
    public CompoundTag toTag(final CompoundTag data) {
        super.toTag(data);
        this.manager.writeToNBT(data);
        this.upgrades.writeToNBT(data, "upgrades");
        data.putInt("lastRedstoneState", this.lastRedstoneState.ordinal());
        return data;
    }

    @Override
    public void fromTag(BlockState state, final CompoundTag data) {
        super.fromTag(state, data);
        this.manager.readFromNBT(data);
        this.upgrades.readFromNBT(data, "upgrades");
        if (data.contains("lastRedstoneState")) {
            this.lastRedstoneState = YesNo.values()[data.getInt("lastRedstoneState")];
        }
    }

    @Override
    public AECableType getCableConnectionType(final AEPartLocation dir) {
        return AECableType.SMART;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    private void updateTask() {
        try {
            if (this.hasWork()) {
                this.getProxy().getTick().wakeDevice(this.getProxy().getNode());
            } else {
                this.getProxy().getTick().sleepDevice(this.getProxy().getNode());
            }
        } catch (final GridAccessException e) {
            // :P
        }
    }

    public void updateRedstoneState() {
        final YesNo currentState = this.world.getReceivedRedstonePower(this.pos) != 0 ? YesNo.YES : YesNo.NO;
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
        if (this.getInstalledUpgrades(Upgrades.REDSTONE) == 0) {
            return true;
        }

        final RedstoneMode rs = (RedstoneMode) this.manager.getSetting(Settings.REDSTONE_CONTROLLED);
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
    public FixedItemInv getInventoryByName(final String name) {
        if (name.equals("upgrades")) {
            return this.upgrades;
        }

        if (name.equals("cells")) {
            return this.combinedInventory;
        }

        return null;
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Settings settingName, final Enum<?> newValue) {
        this.updateTask();
    }

    private boolean hasWork() {
        if (this.isEnabled()) {
            return !ItemHandlerUtil.isEmpty(this.inputCells);
        }

        return false;
    }

    @Override
    public FixedItemInv getInternalInventory() {
        return this.combinedInventory;
    }

    @Override
    public void onChangeInventory(final FixedItemInv inv, final int slot, final InvOperation mc,
            final ItemStack removed, final ItemStack added) {
        if (this.inputCells == inv) {
            this.updateTask();
        }
    }

    @Override
    protected FixedItemInv getItemHandlerForSide(final Direction facing) {
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
        if (!this.getProxy().isActive()) {
            return TickRateModulation.IDLE;
        }

        TickRateModulation ret = TickRateModulation.SLEEP;
        long itemsToMove = 256;

        switch (this.getInstalledUpgrades(Upgrades.SPEED)) {
            case 1:
                itemsToMove *= 2;
                break;
            case 2:
                itemsToMove *= 4;
                break;
            case 3:
                itemsToMove *= 8;
                break;
        }

        try {
            final IEnergySource energy = this.getProxy().getEnergy();
            for (int x = 0; x < NUMBER_OF_CELL_SLOTS; x++) {
                final ItemStack is = this.inputCells.getInvStack(x);
                if (!is.isEmpty()) {
                    boolean shouldMove = true;

                    for (IStorageChannel<? extends IAEStack<?>> c : Api.instance().storage().storageChannels()) {
                        if (itemsToMove > 0) {
                            final IMEMonitor<? extends IAEStack<?>> network = this.getProxy().getStorage()
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
                        ret = TickRateModulation.URGENT;
                    } else {
                        ret = TickRateModulation.URGENT;
                    }

                }
            }
        } catch (final GridAccessException e) {
            ret = TickRateModulation.IDLE;
        }

        return ret;
    }

    @Override
    public int getInstalledUpgrades(final Upgrades u) {
        return this.upgrades.getInstalledUpgrades(u);
    }

    private IMEInventory<?> getInv(final ItemStack is, final IStorageChannel<?> chan) {
        if (this.currentCell != is) {
            this.currentCell = is;
            this.cachedInventories = new IdentityHashMap<>();

            for (IStorageChannel<? extends IAEStack<?>> c : Api.instance().storage().storageChannels()) {
                this.cachedInventories.put(c, Api.instance().registries().cell().getCellInventory(is, null, c));
            }
        }

        return this.cachedInventories.get(chan);
    }

    private long transferContents(final IEnergySource energy, final IMEInventory src, final IMEInventory destination,
            long itemsToMove, final IStorageChannel chan) {
        final IItemList<? extends IAEStack> myList;
        if (src instanceof IMEMonitor) {
            myList = ((IMEMonitor) src).getStorageList();
        } else {
            myList = src.getAvailableItems(src.getChannel().createList());
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
        final FullnessMode fm = (FullnessMode) this.manager.getSetting(Settings.FULLNESS_MODE);

        if (inv != null) {
            return this.matches(fm, inv);
        }

        return true;
    }

    private boolean moveSlot(final int x) {
        final InventoryAdaptor ad = new AdaptorFixedInv(this.outputCells);
        if (ad.addItems(this.inputCells.getInvStack(x)).isEmpty()) {
            this.inputCells.setInvStack(x, ItemStack.EMPTY, Simulation.ACTION);
            return true;
        }
        return false;
    }

    private boolean matches(final FullnessMode fm, final IMEInventory src) {
        if (fm == FullnessMode.HALF) {
            return true;
        }

        final IItemList<? extends IAEStack> myList;

        if (src instanceof IMEMonitor) {
            myList = ((IMEMonitor) src).getStorageList();
        } else {
            myList = src.getAvailableItems(src.getChannel().createList());
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
     * @param w     world
     * @param pos   pos of block entity
     * @param drops drops of block entity
     */
    @Override
    public void getDrops(final World w, final BlockPos pos, final List<ItemStack> drops) {
        super.getDrops(w, pos, drops);

        for (int upgradeIndex = 0; upgradeIndex < this.upgrades.getSlotCount(); upgradeIndex++) {
            final ItemStack stackInSlot = this.upgrades.getInvStack(upgradeIndex);

            if (!stackInSlot.isEmpty()) {
                drops.add(stackInSlot);
            }
        }
    }
}
