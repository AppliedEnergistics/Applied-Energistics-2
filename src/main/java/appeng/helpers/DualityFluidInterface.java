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

package appeng.helpers;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackList;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalBlockPos;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.me.storage.NullInventory;
import appeng.me.storage.StorageAdapter;
import appeng.util.ConfigManager;
import appeng.util.IVariantConversion;
import appeng.util.Platform;
import appeng.util.fluid.AEFluidInventory;
import appeng.util.fluid.IAEFluidTank;
import appeng.util.inv.IAEFluidInventory;

public class DualityFluidInterface
        extends DualityInterface
        implements IAEFluidInventory, IConfigurableObject, IConfigurableFluidInventory {
    public static final int NUMBER_OF_TANKS = 6;
    public static final long TANK_CAPACITY = FluidConstants.BUCKET * 4;

    private final ConfigManager cm = new ConfigManager((manager, setting) -> {
        saveChanges();
    });
    private boolean hasConfig = false;
    private final AEFluidInventory tanks = new AEFluidInventory(this, NUMBER_OF_TANKS, TANK_CAPACITY);
    private final AEFluidInventory config = new AEFluidInventory(this, NUMBER_OF_TANKS);
    private final IAEFluidStack[] requireWork;
    @Nullable
    private InterfaceInventory localInvHandler;
    private int isWorking = -1;

    public DualityFluidInterface(IManagedGridNode gridNode, IFluidInterfaceHost ih) {
        super(gridNode, ih);

        this.requireWork = new IAEFluidStack[NUMBER_OF_TANKS];
        for (int i = 0; i < NUMBER_OF_TANKS; ++i) {
            this.requireWork[i] = null;
        }
    }

    /**
     * Returns an ME compatible monitor for the interfaces local storage.
     */
    @Override
    protected <T extends IAEStack> IMEMonitor<T> getLocalInventory(IStorageChannel<T> channel) {
        if (channel == StorageChannels.fluids()) {
            if (localInvHandler == null) {
                localInvHandler = new InterfaceInventory();
            }
            return localInvHandler.cast(channel);
        }
        return null;
    }

    public void notifyNeighbors() {
        if (this.mainNode.isActive()) {
            this.mainNode.ifPresent((grid, node) -> {
                grid.getTickManager().wakeDevice(node);
            });
        }

        final BlockEntity te = this.host.getBlockEntity();
        if (te != null && te.getLevel() != null) {
            Platform.notifyBlocksOfNeighbors(te.getLevel(), te.getBlockPos());
        }
    }

    public void gridChanged() {
        var grid = mainNode.getGrid();
        if (grid != null) {
            this.items.setInternal(grid.getStorageService()
                    .getInventory(StorageChannels.items()));
            this.fluids.setInternal(grid.getStorageService()
                    .getInventory(StorageChannels.fluids()));
        } else {
            this.items.setInternal(NullInventory.of(StorageChannels.items()));
            this.fluids.setInternal(NullInventory.of(StorageChannels.fluids()));
        }

        this.notifyNeighbors();
    }

    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    public DimensionalBlockPos getLocation() {
        return new DimensionalBlockPos(this.host.getBlockEntity());
    }

    private void readConfig() {
        this.hasConfig = false;

        for (int i = 0; i < this.config.getSlots(); i++) {
            if (this.config.getFluidInSlot(i) != null) {
                this.hasConfig = true;
                break;
            }
        }

        final boolean had = this.hasWorkToDo();

        for (int x = 0; x < NUMBER_OF_TANKS; x++) {
            this.updatePlan(x);
        }

        final boolean has = this.hasWorkToDo();

        if (had != has) {
            mainNode.ifPresent((grid, node) -> {
                if (has) {
                    grid.getTickManager().alertDevice(node);
                } else {
                    grid.getTickManager().sleepDevice(node);
                }
            });
        }

        this.notifyNeighbors();
    }

    @Override
    protected boolean updateStorage() {
        boolean didSomething = false;
        for (int x = 0; x < NUMBER_OF_TANKS; x++) {
            if (this.requireWork[x] != null) {
                didSomething = this.usePlan(x) || didSomething;
            }
        }
        return didSomething;
    }

    @Override
    protected boolean hasConfig() {
        return this.hasConfig;
    }

    @Override
    protected boolean hasWorkToDo() {
        for (var requiredWork : this.requireWork) {
            if (requiredWork != null) {
                return true;
            }
        }

        return false;
    }

    private void updatePlan(final int slot) {
        final IAEFluidStack req = this.config.getFluidInSlot(slot);
        final IAEFluidStack stored = this.tanks.getFluidInSlot(slot);

        if (req == null && stored != null && stored.getStackSize() > 0) {
            var work = stored.copy();
            work.setStackSize(-work.getStackSize());
            this.requireWork[slot] = work;
            return;
        } else if (req != null) {
            if (stored == null || stored.getStackSize() == 0) // need to add stuff!
            {
                this.requireWork[slot] = req.copy();
                this.requireWork[slot].setStackSize(TANK_CAPACITY);
                return;
            } else if (req.equals(stored)) // same type ( qty different? )!
            {
                if (stored.getStackSize() < TANK_CAPACITY) {
                    this.requireWork[slot] = req.copy();
                    this.requireWork[slot].setStackSize(TANK_CAPACITY - stored.getStackSize());
                    return;
                }
            } else
            // Stored != null; dispose!
            {
                var work = stored.copy();
                work.setStackSize(-work.getStackSize());
                this.requireWork[slot] = work;
                return;
            }
        }

        this.requireWork[slot] = null;
    }

    private boolean usePlan(final int slot) {
        IAEFluidStack work = this.requireWork[slot];
        this.isWorking = slot;

        boolean changed = false;
        var grid = this.mainNode.getGrid();
        if (grid != null) {
            final IMEInventory<IAEFluidStack> dest = grid.getStorageService()
                    .getInventory(StorageChannels.fluids());
            final IEnergySource src = grid.getEnergyService();

            if (work.getStackSize() > 0) {
                // make sure strange things didn't happen...
                if (this.tanks.fill(slot, work, false) != work.getStackSize()) {
                    changed = true;
                } else {
                    final IAEFluidStack acquired = StorageHelper.poweredExtraction(src, dest, work,
                            this.interfaceRequestSource);
                    if (acquired != null) {
                        changed = true;
                        final long filled = this.tanks.fill(slot, acquired, true);
                        if (filled != acquired.getStackSize()) {
                            throw new IllegalStateException("bad attempt at managing tanks. ( fill )");
                        }
                    }
                }
            } else if (work.getStackSize() < 0) {
                IAEFluidStack toStore = work.copy();
                toStore.setStackSize(-toStore.getStackSize());

                // make sure strange things didn't happen...
                final long canExtract = this.tanks.drain(slot, toStore, false);
                if (canExtract != toStore.getStackSize()) {
                    changed = true;
                } else {
                    IAEFluidStack notStored = StorageHelper.poweredInsert(src, dest, toStore,
                            this.interfaceRequestSource);
                    toStore.setStackSize(toStore.getStackSize() - (notStored == null ? 0 : notStored.getStackSize()));

                    if (toStore.getStackSize() > 0) {
                        // extract items!
                        changed = true;
                        final long removed = this.tanks.drain(slot, toStore, true);
                        if (toStore.getStackSize() != removed) {
                            throw new IllegalStateException("bad attempt at managing tanks. ( drain )");
                        }
                    }
                }
            }
        }

        if (changed) {
            this.updatePlan(slot);
        }

        this.isWorking = -1;
        return changed;
    }

    @Override
    public void onFluidInventoryChanged(final IAEFluidTank inventory, final int slot) {
        if (this.isWorking == slot) {
            return;
        }

        if (inventory == this.config) {
            this.readConfig();
        } else if (inventory == this.tanks) {
            this.saveChanges();

            final boolean had = this.hasWorkToDo();

            this.updatePlan(slot);

            final boolean now = this.hasWorkToDo();

            if (had != now) {
                mainNode.ifPresent((grid, node) -> {
                    if (now) {
                        grid.getTickManager().alertDevice(node);
                    } else {
                        grid.getTickManager().sleepDevice(node);
                    }
                });
            }
        }
    }

    @Override
    public boolean isRemote() {
        Level level = this.host.getBlockEntity().getLevel();
        return level == null || level.isClientSide();
    }

    @Override
    public void writeToNBT(final CompoundTag data) {
        super.writeToNBT(data);
        this.tanks.writeToNBT(data, "storage");
        this.config.writeToNBT(data, "config");
    }

    @Override
    public void readFromNBT(final CompoundTag data) {
        super.readFromNBT(data);
        this.config.readFromNBT(data, "config");
        this.tanks.readFromNBT(data, "storage");
        this.readConfig();
    }

    public IAEFluidTank getConfig() {
        return this.config;
    }

    public IAEFluidTank getTanks() {
        return this.tanks;
    }

    private class InterfaceInventory extends StorageAdapter<FluidVariant, IAEFluidStack>
            implements IMEMonitor<IAEFluidStack> {

        InterfaceInventory() {
            super(IVariantConversion.FLUID, tanks,
                    cm.getSetting(Settings.STORAGE_FILTER) == StorageFilter.EXTRACTABLE_ONLY);
            this.setActionSource(actionSource);
        }

        @Override
        public IAEFluidStack injectItems(final IAEFluidStack input, final Actionable type, final IActionSource src) {
            if (getRequestInterfacePriority(src).isPresent()) {
                return input;
            }

            return super.injectItems(input, type, src);
        }

        @Override
        public IAEFluidStack extractItems(final IAEFluidStack request, final Actionable type, final IActionSource src) {
            var requestPriority = getRequestInterfacePriority(src);
            if (requestPriority.isPresent() && requestPriority.getAsInt() <= getPriority()) {
                return null;
            }

            return super.extractItems(request, type, src);
        }

        @Override
        protected void onInjectOrExtract() {
            // Rebuild cache immediately
            this.onTick();
        }

        @Override
        public IAEStackList<IAEFluidStack> getStorageList() {
            return getAvailableItems();
        }
    }

    public void saveChanges() {
        this.host.saveChanges();
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.cm;
    }

    @Override
    public Storage<FluidVariant> getFluidInventoryByName(final String name) {
        if (name.equals("config")) {
            return this.config;
        }
        return null;
    }

}
