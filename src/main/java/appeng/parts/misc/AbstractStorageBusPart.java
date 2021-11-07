/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.parts.misc;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

import appeng.api.config.*;
import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.storage.*;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.core.settings.TickRates;
import appeng.core.stats.AdvancementTriggers;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.IHandlerAdapter;
import appeng.me.storage.ITickingMonitor;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.NullInventory;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.parts.PartAdjacentApi;
import appeng.parts.automation.UpgradeablePart;
import appeng.util.prioritylist.FuzzyPriorityList;
import appeng.util.prioritylist.PrecisePriorityList;

/**
 * @param <A> "API class" of the handler, i.e. IItemHandler or IFluidHandler.
 */
public abstract class AbstractStorageBusPart<T extends IAEStack, A> extends UpgradeablePart
        implements IGridTickable, IStorageProvider, IPriorityHost {
    protected final IActionSource source;
    private final TickRates tickRates;
    /**
     * This is the virtual inventory this storage bus exposes to the network it belongs to. To avoid continuous
     * cell-change notifications, we instead use a handler that will exist as long as this storage bus exists, while
     * changing the underlying inventory.
     */
    private final MEInventoryHandler<T> handler = new MEInventoryHandler<>(NullInventory.of(getStorageChannel()));
    /**
     * Listener for listening to changes in an {@link IMEMonitor} if this storage bus is attached to an interface.
     */
    private final Listener listener = new Listener();
    private final PartAdjacentApi<IStorageMonitorableAccessor> adjacentStorageAccessor;
    private final PartAdjacentApi<A> adjacentExternalApi;
    private boolean wasActive = false;
    private int priority = 0;
    /**
     * Indicates that the storage-bus should reevaluate the block it's attached to on the next tick and update the
     * target inventory - if necessary.
     */
    private boolean shouldUpdateTarget = true;
    private ITickingMonitor monitor = null;

    public AbstractStorageBusPart(TickRates tickRates, ItemStack is, BlockApiLookup<A, Direction> apiLookup) {
        super(is);
        this.adjacentStorageAccessor = new PartAdjacentApi<>(this, IStorageMonitorableAccessor.SIDED);
        this.adjacentExternalApi = new PartAdjacentApi<>(this, apiLookup);
        this.tickRates = tickRates;
        this.getConfigManager().registerSetting(Settings.ACCESS, AccessRestriction.READ_WRITE);
        this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.getConfigManager().registerSetting(Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY);
        this.getConfigManager().registerSetting(Settings.FILTER_ON_EXTRACT, YesNo.YES);
        this.source = new MachineSource(this);
        getMainNode()
                .addService(IStorageProvider.class, this)
                .addService(IGridTickable.class, this);
    }

    protected abstract IStorageChannel<T> getStorageChannel();

    protected abstract IMEInventory<T> adaptExternalApi(A handler, boolean extractableOnly, Runnable alertDevice);

    protected abstract int getStackConfigSize();

    @Nullable
    protected abstract T getStackInConfigSlot(int slot);

    @Override
    protected final void onMainNodeStateChanged(IGridNodeListener.State reason) {
        var currentActive = this.getMainNode().isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            this.getHost().markForUpdate();
            remountStorage();
        }
    }

    private void remountStorage() {
        IStorageProvider.requestUpdate(getMainNode());
    }

    @Override
    public void onSettingChanged(IConfigManager manager, Setting<?> setting) {
        this.onConfigurationChanged();
        this.getHost().markForSave();
    }

    @Override
    public final void upgradesChanged() {
        super.upgradesChanged();
        this.onConfigurationChanged();
    }

    /**
     * Schedule a re-evaluation of the target inventory on the next tick alert the device in case its sleeping.
     */
    private void scheduleUpdate() {
        if (isRemote()) {
            return;
        }

        this.shouldUpdateTarget = true;
        getMainNode().ifPresent((grid, node) -> {
            grid.getTickManager().alertDevice(node);
        });
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        this.priority = data.getInt("priority");
    }

    @Override
    public void writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putInt("priority", this.priority);
    }

    @Override
    public final boolean onPartActivate(final Player player, final InteractionHand hand, final Vec3 pos) {
        if (!isRemote()) {
            MenuOpener.open(getMenuType(), player, MenuLocator.forPart(this));
        }
        return true;
    }

    @Override
    public final void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(3, 3, 15, 13, 13, 16);
        bch.addBox(2, 2, 14, 14, 14, 15);
        bch.addBox(5, 5, 12, 11, 11, 14);
    }

    @Override
    protected final int getUpgradeSlots() {
        return 5;
    }

    @Override
    public final float getCableConnectionLength(AECableType cable) {
        return 4;
    }

    @Override
    public final void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        if (pos.relative(getSide()).equals(neighbor)) {
            var te = level.getBlockEntity(neighbor);

            if (te == null) {
                // In case the TE was destroyed, we have to update the target handler immediately.
                this.updateTarget(false);
            } else {
                this.scheduleUpdate();
            }
        }
    }

    @Override
    public final TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(tickRates.getMin(), tickRates.getMax(), false, true);
    }

    @Override
    public final TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        if (this.shouldUpdateTarget) {
            this.updateTarget(false);
        }

        if (this.monitor != null) {
            return this.monitor.onTick();
        }

        return TickRateModulation.SLEEP;
    }

    /**
     * Used by the menu to configure based on stored contents.
     */
    public IMEInventory<T> getInternalHandler() {
        return this.handler.getInventory();
    }

    private boolean hasRegisteredCellToNetwork() {
        return this.isActive() && !(this.handler.getInventory() instanceof NullInventory);
    }

    protected void onConfigurationChanged() {
        updateTarget(true);
    }

    private void updateTarget(boolean forceFullUpdate) {
        boolean wasInventory = this.handler.getInventory() instanceof IHandlerAdapter;
        IMEMonitor<T> foundMonitor = null;
        A foundExternalApi = null;

        // Prioritize a handler to directly link to another ME network
        var accessor = adjacentStorageAccessor.find();

        if (accessor != null) {
            var inventory = accessor.getInventory(this.source);
            if (inventory != null) {
                foundMonitor = inventory.getInventory(getStorageChannel());
            }

            // So this could / can be a design decision. If the block entity does support our custom capability,
            // but it does not return an inventory for the action source, we do NOT fall back to using the external
            // API, as that might circumvent the security settings, and might also cause performance issues.
        } else {
            foundExternalApi = adjacentExternalApi.find();
        }

        if (!forceFullUpdate && wasInventory && foundExternalApi != null) {
            // Just update the inventory reference, the ticking monitor will take care of the rest.
            ((IHandlerAdapter<A>) this.handler.getInventory()).setHandler(foundExternalApi);
        } else if (!forceFullUpdate && foundMonitor == this.handler.getInventory()) {
            // Monitor didn't change, nothing to do!
        } else {
            var wasSleeping = this.monitor == null;
            var wasRegistered = this.hasRegisteredCellToNetwork();
            var before = wasRegistered ? this.handler.getAvailableStacks() : null;

            // Update inventory
            boolean extractableOnly = this.getConfigManager()
                    .getSetting(Settings.STORAGE_FILTER) == StorageFilter.EXTRACTABLE_ONLY;
            IMEInventory<T> newInventory;
            if (foundMonitor != null) {
                newInventory = foundMonitor;
                this.checkStorageBusOnInterface();
            } else if (foundExternalApi != null) {
                newInventory = adaptExternalApi(foundExternalApi, extractableOnly, () -> {
                    getMainNode().ifPresent((grid, node) -> {
                        grid.getTickManager().alertDevice(node);
                    });
                });
            } else {
                newInventory = NullInventory.of(getStorageChannel());
            }
            this.handler.setInventory(newInventory);

            // Apply other settings.
            this.handler.setMaxAccess(this.getConfigManager().getSetting(Settings.ACCESS));
            this.handler.setWhitelist(getInstalledUpgrades(Upgrades.INVERTER) > 0 ? IncludeExclude.BLACKLIST
                    : IncludeExclude.WHITELIST);
            this.handler.setPriority(this.priority);

            var priorityList = getStorageChannel().createList();

            var slotsToUse = 18 + getInstalledUpgrades(Upgrades.CAPACITY) * 9;
            for (var x = 0; x < getStackConfigSize() && x < slotsToUse; x++) {
                var is = getStackInConfigSlot(x);
                if (is != null) {
                    priorityList.add(is);
                }
            }

            if (getInstalledUpgrades(Upgrades.FUZZY) > 0) {
                this.handler.setPartitionList(new FuzzyPriorityList<>(priorityList,
                        this.getConfigManager().getSetting(Settings.FUZZY_MODE)));
            } else {
                this.handler.setPartitionList(new PrecisePriorityList<>(priorityList));
            }

            // Ensure we apply the partition list to the available items.
            boolean filterOnExtract = this.getConfigManager().getSetting(Settings.FILTER_ON_EXTRACT) == YesNo.YES;
            this.handler.setExtractFiltering(filterOnExtract, extractableOnly && filterOnExtract);

            // First, post the change.
            if (wasRegistered) {
                var after = this.handler.getAvailableStacks();
                StorageHelper.postListChanges(before, after, listener, this.source);
            }

            // Let the new inventory react to us ticking.
            if (newInventory instanceof ITickingMonitor tickingMonitor) {
                this.monitor = tickingMonitor;
                tickingMonitor.setActionSource(this.source);
            } else {
                this.monitor = null;
            }

            // Notify the network of any external change to the inventory.
            if (newInventory instanceof IMEMonitor<T>monitor) {
                monitor.addListener(listener, newInventory);
            }

            // Update sleeping state.
            if (wasSleeping != (this.monitor == null)) {
                getMainNode().ifPresent((grid, node) -> {
                    var tm = grid.getTickManager();
                    if (this.monitor == null) {
                        tm.sleepDevice(node);
                    } else {
                        tm.wakeDevice(node);
                    }
                });
            }

            if (wasRegistered != this.hasRegisteredCellToNetwork()) {
                remountStorage();
            }
        }
    }

    private void checkStorageBusOnInterface() {
        var oppositeSide = getSide().getOpposite();
        var targetPos = getBlockEntity().getBlockPos().relative(getSide());
        var targetBe = getLevel().getBlockEntity(targetPos);

        Object targetHost = targetBe;
        if (targetBe instanceof IPartHost partHost) {
            targetHost = partHost.getPart(oppositeSide);
        }

        if (targetHost instanceof IInterfaceHost) {
            var server = getLevel().getServer();
            var player = IPlayerRegistry.getConnected(server, this.getActionableNode().getOwningPlayerId());
            if (player != null) {
                AdvancementTriggers.RECURSIVE.trigger(player);
            }
        }
    }

    @Override
    public void mountInventories(IStorageMounts mounts) {
        if (this.hasRegisteredCellToNetwork()) {
            mounts.mount(this.handler, priority);
        }
    }

    @Override
    public final int getPriority() {
        return this.priority;
    }

    @Override
    public final void setPriority(final int newValue) {
        this.priority = newValue;
        this.getHost().markForSave();
        this.remountStorage();
    }

    class Listener implements IMEMonitorListener<T> {
        @Override
        public final boolean isValid(Object verificationToken) {
            return handler.getInventory() == verificationToken;
        }

        @Override
        public final void postChange(final IMEMonitor<T> monitor, final Iterable<T> change,
                final IActionSource source) {
            if (getMainNode().isActive()) {
                getMainNode().ifPresent((grid, node) -> {
                    grid.getStorageService().postAlterationOfStoredItems(getStorageChannel(), change, source);
                });
            }
        }

        @Override
        public final void onListUpdate() {
            // not used here.
        }
    }

}
