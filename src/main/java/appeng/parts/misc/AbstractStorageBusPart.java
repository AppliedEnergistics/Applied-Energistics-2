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

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;

import appeng.api.config.AccessRestriction;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.events.GridCellArrayUpdate;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.ICellProvider;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.blockentity.misc.ItemInterfaceBlockEntity;
import appeng.capabilities.Capabilities;
import appeng.core.settings.TickRates;
import appeng.helpers.IPriorityHost;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.ITickingMonitor;
import appeng.me.storage.MEInventoryHandler;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.parts.automation.UpgradeablePart;
import appeng.util.Platform;
import appeng.util.prioritylist.FuzzyPriorityList;
import appeng.util.prioritylist.PrecisePriorityList;

/**
 * @param <A> "API class" of the handler, i.e. IItemHandler or IFluidHandler.
 */
public abstract class AbstractStorageBusPart<T extends IAEStack<T>, A> extends UpgradeablePart
        implements IGridTickable, ICellProvider, IMEMonitorHandlerReceiver<T>, IPriorityHost {
    private final Capability<A> handlerCapability;
    protected final IActionSource source;
    private final TickRates tickRates;
    private boolean wasActive = false;
    private int priority = 0;
    private boolean cached = false;
    private ITickingMonitor monitor = null;
    private MEInventoryHandler<T> handler = null;
    /**
     * Last target (the IItemHandler, IFluidHandler or IMEMonitor). If it changes we need to rebuild the handler.
     */
    @Nullable
    private Object lastTargetObject = null;
    private byte resetCacheLogic = 0;
    private static final Object NO_TARGET = new Object();
    private final NonNullConsumer<LazyOptional<A>> apiInvalidationListener = this::apiInvalidated;

    public AbstractStorageBusPart(TickRates tickRates, ItemStack is, Capability<A> handlerCapability) {
        super(is);
        this.handlerCapability = handlerCapability;
        this.tickRates = tickRates;
        this.getConfigManager().registerSetting(Settings.ACCESS, AccessRestriction.READ_WRITE);
        this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.getConfigManager().registerSetting(Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY);
        this.source = new MachineSource(this);
        getMainNode()
                .addService(ICellProvider.class, this)
                .addService(IGridTickable.class, this);
    }

    protected abstract IStorageChannel<T> getStorageChannel();

    @Nullable
    protected abstract IMEInventory<T> getHandlerAdapter(A handler, Runnable alertDevice);

    protected abstract int getStackConfigSize();

    @Nullable
    protected abstract T getStackInConfigSlot(int slot);

    @Override
    protected final void onMainNodeStateChanged(IGridNodeListener.State reason) {
        var currentActive = this.getMainNode().isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            this.getHost().markForUpdate();
            this.getMainNode().ifPresent(grid -> grid.postEvent(new GridCellArrayUpdate()));
        }
    }

    @Override
    public final void updateSetting(IConfigManager manager, Settings settingName, Enum<?> newValue) {
        this.scheduleCacheReset(true);
        this.getHost().markForSave();
    }

    @Override
    public final void upgradesChanged() {
        super.upgradesChanged();
        this.scheduleCacheReset(true);
    }

    protected final void scheduleCacheReset(final boolean fullReset) {
        if (isRemote()) {
            return;
        }

        if (fullReset) {
            this.resetCacheLogic = 2;
        } else {
            this.resetCacheLogic = 1;
        }

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
    public final boolean isValid(final Object verificationToken) {
        return this.handler == verificationToken;
    }

    @Override
    public final void postChange(final IBaseMonitor<T> monitor, final Iterable<T> change, final IActionSource source) {
        if (this.getMainNode().isActive()) {
            getMainNode().ifPresent((grid, node) -> {
                grid.getStorageService().postAlterationOfStoredItems(getStorageChannel(), change, this.source);
            });
        }
    }

    @Override
    public final void onListUpdate() {
        // not used here.
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
        if (pos.relative(this.getSide()).equals(neighbor)) {
            var te = level.getBlockEntity(neighbor);

            // In case the TE was destroyed, we have to do a full reset immediately.
            if (te == null) {
                this.scheduleCacheReset(true);
                this.doCacheReset();
            } else {
                this.scheduleCacheReset(false);
            }
        }
    }

    private void apiInvalidated(LazyOptional<A> aLazyOptional) {
        // Make sure this storage bus still exists.
        if (this.getMainNode().isReady()) {
            // Do a full cache reset if the LazyOptional was invalidated.
            // This is useful in case the tile doesn't trigger a neighbor change event for some reason.
            this.scheduleCacheReset(true);
            this.doCacheReset();
        }
    }

    @Override
    public final TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(tickRates.getMin(), tickRates.getMax(), this.monitor == null, true);
    }

    @Override
    public final TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        if (this.resetCacheLogic != 0) {
            this.doCacheReset();
        }

        if (this.monitor != null) {
            return this.monitor.onTick();
        }

        return TickRateModulation.SLEEP;
    }

    private void doCacheReset() {
        var fullReset = this.resetCacheLogic == 2;
        this.resetCacheLogic = 0;

        final IMEInventory<T> in = this.getInternalHandler();
        var before = getStorageChannel().createList();
        if (in != null) {
            before = in.getAvailableItems(before);
        }

        this.cached = false;
        if (fullReset) {
            this.lastTargetObject = null;
        }

        final IMEInventory<T> out = this.getInternalHandler();

        if (in != out) {
            var after = getStorageChannel().createList();
            if (out != null) {
                after = out.getAvailableItems(after);
            }
            Platform.postListChanges(before, after, this, this.source);
        }
    }

    private IMEInventory<T> getInventoryWrapper(BlockEntity target) {

        var targetSide = this.getSide().getOpposite();

        // Prioritize a handler to directly link to another ME network
        var accessorOpt = target.getCapability(Capabilities.STORAGE_MONITORABLE_ACCESSOR, targetSide);

        if (accessorOpt.isPresent()) {
            var accessor = accessorOpt.orElse(null);
            var inventory = accessor.getInventory(this.source);
            if (inventory != null) {
                return inventory.getInventory(getStorageChannel());
            }

            // So this could / can be a design decision. If the block entity does support our custom
            // capability,
            // but it does not return an inventory for the action source, we do NOT fall
            // back to using
            // IItemHandler's, as that might circumvent the security setings, and might also
            // cause
            // performance issues.
            return null;
        }

        // Check via cap adapter
        var handler = target.getCapability(this.handlerCapability, targetSide).orElse(null);

        if (handler != null) {
            return getHandlerAdapter(handler, () -> {
                getMainNode().ifPresent((grid, node) -> {
                    grid.getTickManager().alertDevice(node);
                });
            });
        } else {
            return null;
        }
    }

    // TODO, LazyOptionals are cacheable this might need changing?
    private Object getTargetObject(BlockEntity target) {
        if (target == null) {
            return 0;
        }

        var targetSide = this.getSide().getOpposite();

        var accessor = target.getCapability(Capabilities.STORAGE_MONITORABLE_ACCESSOR, targetSide).orElse(null);

        if (accessor != null) {
            // The accessor might be the same, but the monitor might have changed (e.g. interface suddenly has config).
            // So we have to return the monitor and not the accessor.
            IMEMonitor<T> targetMonitor = null;
            var monitorable = accessor.getInventory(this.source);

            if (monitorable != null) {
                targetMonitor = monitorable.getInventory(getStorageChannel());
            }

            // Always return: if an IStorageMonitorableAccessor is exposed, we never query the handler capability.
            // Even if the IMEMonitor is null.
            if (targetMonitor == null) {
                // Marks a null monitor - to prevent frequent rebuilds if null is returned.
                return NO_TARGET;
            } else {
                return targetMonitor;
            }
        }

        LazyOptional<A> adjCap = target.getCapability(this.handlerCapability, targetSide);

        if (adjCap.isPresent()) {
            adjCap.addListener(apiInvalidationListener);
            return adjCap.resolve().get();
        }

        return null;
    }

    public final MEInventoryHandler<T> getInternalHandler() {
        if (this.cached) {
            return this.handler;
        }

        var wasSleeping = this.monitor == null;

        this.cached = true;
        var self = this.getHost().getBlockEntity();
        var target = self.getLevel().getBlockEntity(self.getBlockPos().relative(this.getSide()));
        var newTargetObject = this.getTargetObject(target);

        if (newTargetObject != null && newTargetObject == this.lastTargetObject) {
            return this.handler;
        }

        this.lastTargetObject = newTargetObject;
        this.handler = null;
        this.monitor = null;
        if (target != null) {
            var inv = this.getInventoryWrapper(target);

            if (inv instanceof ITickingMonitor tickingMonitor) {
                this.monitor = tickingMonitor;
                this.monitor.setActionSource(new MachineSource(this));
            }

            if (inv != null) {
                this.checkInterfaceVsStorageBus(target, this.getSide());

                this.handler = new MEInventoryHandler<>(inv, getStorageChannel());

                this.handler.setBaseAccess((AccessRestriction) this.getConfigManager().getSetting(Settings.ACCESS));
                this.handler.setWhitelist(this.getInstalledUpgrades(Upgrades.INVERTER) > 0 ? IncludeExclude.BLACKLIST
                        : IncludeExclude.WHITELIST);
                this.handler.setPriority(this.priority);

                var priorityList = getStorageChannel().createList();

                var slotsToUse = 18 + this.getInstalledUpgrades(Upgrades.CAPACITY) * 9;
                for (var x = 0; x < getStackConfigSize() && x < slotsToUse; x++) {
                    var is = getStackInConfigSlot(x);
                    if (is != null) {
                        priorityList.add(is);
                    }
                }

                if (this.getInstalledUpgrades(Upgrades.FUZZY) > 0) {
                    this.handler.setPartitionList(new FuzzyPriorityList<>(priorityList,
                            (FuzzyMode) this.getConfigManager().getSetting(Settings.FUZZY_MODE)));
                } else {
                    this.handler.setPartitionList(new PrecisePriorityList<>(priorityList));
                }

                if (inv instanceof IBaseMonitor) {
                    ((IBaseMonitor<T>) inv).addListener(this, this.handler);
                }
            }
        }

        // update sleep state...
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

        // force grid to update handlers...
        this.getMainNode().ifPresent(grid -> grid.postEvent(new GridCellArrayUpdate()));

        return this.handler;
    }

    private void checkInterfaceVsStorageBus(final BlockEntity target, final Direction side) {
        IGridNode targetNode = null;

        if (target instanceof ItemInterfaceBlockEntity interfaceBlockEntity) {
            targetNode = interfaceBlockEntity.getMainNode().getNode();
        } else if (target instanceof IPartHost) {
            var part = ((IPartHost) target).getPart(side);
            if (part instanceof ItemInterfacePart interfacePart) {
                targetNode = interfacePart.getMainNode().getNode();
            }
        }

        if (targetNode != null) {
            // Platform.addStat( achievement.getActionableNode().getPlayerID(),
            // Achievements.Recursive.getAchievement()
            // );
            // Platform.addStat( getActionableNode().getPlayerID(),
            // Achievements.Recursive.getAchievement() );
        }
    }

    @Override
    public final List<IMEInventoryHandler> getCellArray(final IStorageChannel channel) {
        if (channel == getStorageChannel()) {
            var out = this.getMainNode().isActive() ? this.getInternalHandler() : null;
            if (out != null) {
                return Collections.singletonList(out);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public final int getPriority() {
        return this.priority;
    }

    @Override
    public final void setPriority(final int newValue) {
        this.priority = newValue;
        this.getHost().markForSave();
        this.scheduleCacheReset(true);
    }
}
