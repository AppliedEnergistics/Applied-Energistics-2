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

package appeng.helpers;

import java.util.Optional;
import java.util.OptionalInt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.core.settings.TickRates;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.MEMonitorPassThrough;
import appeng.util.Platform;

/**
 * Contains behavior for interface blocks and parts, which is independent of the storage channel.
 */
public abstract class DualityInterface {

    protected final MEMonitorPassThrough<IAEItemStack> items = new MEMonitorPassThrough<>(StorageChannels.items());
    protected final MEMonitorPassThrough<IAEFluidStack> fluids = new MEMonitorPassThrough<>(StorageChannels.fluids());

    protected final IInterfaceHost host;
    protected final IManagedGridNode mainNode;
    protected final IActionSource actionSource;
    protected final IActionSource interfaceRequestSource;
    private final IStorageMonitorableAccessor accessor = this::getMonitorable;
    private int priority;

    public DualityInterface(IManagedGridNode gridNode, IInterfaceHost host) {
        this.host = host;
        this.mainNode = gridNode
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(IGridTickable.class, new Ticker());
        this.actionSource = new MachineSource(mainNode::getNode);

        this.fluids.setChangeSource(this.actionSource);
        this.items.setChangeSource(this.actionSource);

        this.interfaceRequestSource = new InterfaceRequestSource(mainNode::getNode);
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        this.host.saveChanges();
    }

    public void writeToNBT(CompoundTag tag) {
        tag.putInt("priority", this.priority);
    }

    public void readFromNBT(CompoundTag tag) {
        this.priority = tag.getInt("priority");
    }

    public IStorageMonitorableAccessor getGridStorageAccessor() {
        return accessor;
    }

    private class Ticker implements IGridTickable {
        @Override
        public TickingRequest getTickingRequest(final IGridNode node) {
            return new TickingRequest(TickRates.Interface.getMin(), TickRates.Interface.getMax(), !hasWorkToDo(),
                    true);
        }

        @Override
        public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
            if (!mainNode.isActive()) {
                return TickRateModulation.SLEEP;
            }

            boolean couldDoWork = updateStorage();
            return hasWorkToDo() ? couldDoWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER
                    : TickRateModulation.SLEEP;
        }
    }

    /**
     * If the request is for a local inventory operation of an AE interface, returns the priority of that interface.
     */
    protected final OptionalInt getRequestInterfacePriority(IActionSource src) {
        return src.context(InterfaceRequestContext.class)
                .map(ctx -> OptionalInt.of(ctx.getPriority()))
                .orElseGet(OptionalInt::empty);
    }

    protected abstract boolean hasWorkToDo();

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
            this.items.setMonitor(grid.getStorageService().getInventory(StorageChannels.items()));
            this.fluids.setMonitor(grid.getStorageService().getInventory(StorageChannels.fluids()));
        } else {
            this.items.setMonitor(null);
            this.fluids.setMonitor(null);
        }

        this.notifyNeighbors();
    }

    protected abstract boolean updateStorage();

    /**
     * @return True if the interface is configured to stock certain types of resources.
     */
    protected abstract boolean hasConfig();

    private IStorageMonitorable getMonitorable(IActionSource src) {
        // If the given action source can access the grid, return the real inventory
        if (Platform.canAccess(mainNode, src)) {
            return this::getInventory;
        }

        // Otherwise, return a fallback that only exposes the local interface inventory
        return this::getLocalInventory;
    }

    /**
     * Gets the inventory that is exposed to an ME compatible API user if they have access to the grid this interface is
     * a part of. This is normally accessed by storage buses.
     * <p/>
     * If the interface has configured slots, it will <b>always</b> expose its local inventory instead of the grid's
     * inventory.
     */
    private <T extends IAEStack> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
        if (hasConfig()) {
            return getLocalInventory(channel);
        }

        if (channel == StorageChannels.items()) {
            return this.items.cast(channel);
        } else if (channel == StorageChannels.fluids()) {
            return this.fluids.cast(channel);
        }

        return null;
    }

    /**
     * Returns an ME compatible monitor for the interface's local storage for a given storage channel.
     */
    protected abstract <T extends IAEStack> IMEMonitor<T> getLocalInventory(IStorageChannel<T> channel);

    private class InterfaceRequestSource extends MachineSource {
        private final InterfaceRequestContext context;

        InterfaceRequestSource(IActionHost v) {
            super(v);
            this.context = new InterfaceRequestContext();
        }

        @Override
        public <T> Optional<T> context(Class<T> key) {
            if (key == InterfaceRequestContext.class) {
                return Optional.of(key.cast(this.context));
            }

            return super.context(key);
        }
    }

    private class InterfaceRequestContext {
        public int getPriority() {
            return priority;
        }
    }

}
