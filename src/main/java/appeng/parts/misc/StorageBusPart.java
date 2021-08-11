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
import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

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
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.cells.ICellProvider;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigManager;
import appeng.blockentity.inventory.AppEngInternalAEInventory;
import appeng.blockentity.misc.ItemInterfaceBlockEntity;
import appeng.capabilities.Capabilities;
import appeng.core.AppEng;
import appeng.core.definitions.AEParts;
import appeng.core.settings.TickRates;
import appeng.helpers.IPriorityHost;
import appeng.items.parts.PartModels;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.ITickingMonitor;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.MEMonitorIInventory;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.ItemStorageBusMenu;
import appeng.parts.PartModel;
import appeng.parts.automation.UpgradeablePart;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;
import appeng.util.prioritylist.FuzzyPriorityList;
import appeng.util.prioritylist.PrecisePriorityList;

public class StorageBusPart extends UpgradeablePart
        implements IGridTickable, ICellProvider, IMEMonitorHandlerReceiver<IAEItemStack>, IPriorityHost {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "part/item_storage_bus_base");

    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/item_storage_bus_off"));

    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/item_storage_bus_on"));

    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/item_storage_bus_has_channel"));

    private final IActionSource mySrc;
    private final AppEngInternalAEInventory Config = new AppEngInternalAEInventory(this, 63);
    private int priority = 0;
    private boolean cached = false;
    private ITickingMonitor monitor = null;
    private MEInventoryHandler<IAEItemStack> handler = null;
    private int handlerHash = 0;
    private boolean wasActive = false;
    private byte resetCacheLogic = 0;

    public StorageBusPart(final ItemStack is) {
        super(is);
        this.getConfigManager().registerSetting(Settings.ACCESS, AccessRestriction.READ_WRITE);
        this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.getConfigManager().registerSetting(Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY);
        this.mySrc = new MachineSource(this);
        getMainNode()
                .addService(ICellProvider.class, this)
                .addService(IGridTickable.class, this);
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        updateStatus();
    }

    private void updateStatus() {
        final boolean currentActive = this.getMainNode().isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            this.getHost().markForUpdate();
            this.getMainNode().ifPresent(grid -> grid.postEvent(new GridCellArrayUpdate()));
        }
    }

    @Override
    protected int getUpgradeSlots() {
        return 5;
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Settings settingName, final Enum<?> newValue) {
        this.resetCache(true);
        this.getHost().markForSave();
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removedStack, final ItemStack newStack) {
        super.onChangeInventory(inv, slot, mc, removedStack, newStack);

        if (inv == this.Config) {
            this.resetCache(true);
        }
    }

    @Override
    public void upgradesChanged() {
        super.upgradesChanged();
        this.resetCache(true);
    }

    @Override
    public void readFromNBT(final CompoundTag data) {
        super.readFromNBT(data);
        this.Config.readFromNBT(data, "config");
        this.priority = data.getInt("priority");
    }

    @Override
    public void writeToNBT(final CompoundTag data) {
        super.writeToNBT(data);
        this.Config.writeToNBT(data, "config");
        data.putInt("priority", this.priority);
    }

    @Override
    public IItemHandler getInventoryByName(final String name) {
        if (name.equals("config")) {
            return this.Config;
        }

        return super.getInventoryByName(name);
    }

    private void resetCache(final boolean fullReset) {
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
    public boolean isValid(final Object verificationToken) {
        return this.handler == verificationToken;
    }

    @Override
    public void postChange(final IBaseMonitor<IAEItemStack> monitor, final Iterable<IAEItemStack> change,
            final IActionSource source) {
        if (this.getMainNode().isActive()) {
            getMainNode().ifPresent((grid, node) -> {
                grid.getStorageService().postAlterationOfStoredItems(
                        StorageChannels.items(), change, this.mySrc);
            });
        }
    }

    @Override
    public void onListUpdate() {
        // not used here.
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(3, 3, 15, 13, 13, 16);
        bch.addBox(2, 2, 14, 14, 14, 15);
        bch.addBox(5, 5, 12, 11, 11, 14);
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        if (pos.relative(this.getSide().getDirection()).equals(neighbor)) {
            final BlockEntity te = level.getBlockEntity(neighbor);

            // In case the TE was destroyed, we have to do a full reset immediately.
            if (te == null) {
                this.resetCache(true);
                this.resetCache();
            } else {
                this.resetCache(false);
            }
        }
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 4;
    }

    @Override
    public boolean onPartActivate(final Player player, final InteractionHand hand, final Vec3 pos) {
        if (!isRemote()) {
            MenuOpener.open(ItemStorageBusMenu.TYPE, player, MenuLocator.forPart(this));
        }
        return true;
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(TickRates.StorageBus.getMin(), TickRates.StorageBus.getMax(), this.monitor == null,
                true);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        if (this.resetCacheLogic != 0) {
            this.resetCache();
        }

        if (this.monitor != null) {
            return this.monitor.onTick();
        }

        return TickRateModulation.SLEEP;
    }

    private void resetCache() {
        final boolean fullReset = this.resetCacheLogic == 2;
        this.resetCacheLogic = 0;

        final IMEInventory<IAEItemStack> in = this.getInternalHandler();
        IItemList<IAEItemStack> before = StorageChannels.items()
                .createList();
        if (in != null) {
            before = in.getAvailableItems(before);
        }

        this.cached = false;
        if (fullReset) {
            this.handlerHash = 0;
        }

        final IMEInventory<IAEItemStack> out = this.getInternalHandler();

        if (in != out) {
            IItemList<IAEItemStack> after = StorageChannels.items()
                    .createList();
            if (out != null) {
                after = out.getAvailableItems(after);
            }
            Platform.postListChanges(before, after, this, this.mySrc);
        }
    }

    private IMEInventory<IAEItemStack> getInventoryWrapper(BlockEntity target) {

        Direction targetSide = this.getSide().getDirection().getOpposite();

        // Prioritize a handler to directly link to another ME network
        final LazyOptional<IStorageMonitorableAccessor> accessorOpt = target
                .getCapability(Capabilities.STORAGE_MONITORABLE_ACCESSOR, targetSide);

        if (accessorOpt.isPresent()) {
            IStorageMonitorableAccessor accessor = accessorOpt.orElse(null);
            IStorageMonitorable inventory = accessor.getInventory(this.mySrc);
            if (inventory != null) {
                return inventory.getInventory(StorageChannels.items());
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

        // Check via cap for IItemHandler
        final LazyOptional<IItemHandler> handlerExtOpt = target
                .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, targetSide);
        if (handlerExtOpt.isPresent()) {
            return new ItemHandlerAdapter(handlerExtOpt.orElse(null), () -> {
                getMainNode().ifPresent((grid, node) -> {
                    grid.getTickManager().alertDevice(node);
                });
            });
        }

        return null;

    }

    // TODO, LazyOptionals are cacheable this might need changing?
    private int createHandlerHash(BlockEntity target) {
        if (target == null) {
            return 0;
        }

        final Direction targetSide = this.getSide().getDirection().getOpposite();

        final LazyOptional<IStorageMonitorableAccessor> accessorOpt = target
                .getCapability(Capabilities.STORAGE_MONITORABLE_ACCESSOR, targetSide);

        if (accessorOpt.isPresent()) {
            return Objects.hash(target, accessorOpt.orElse(null));
        }

        final LazyOptional<IItemHandler> itemHandlerOpt = target
                .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, targetSide);

        if (itemHandlerOpt.isPresent()) {
            IItemHandler itemHandler = itemHandlerOpt.orElse(null);
            return Objects.hash(target, itemHandler, itemHandler.getSlots());
        }

        return 0;
    }

    public MEInventoryHandler<IAEItemStack> getInternalHandler() {
        if (this.cached) {
            return this.handler;
        }

        final boolean wasSleeping = this.monitor == null;

        this.cached = true;
        final BlockEntity self = this.getHost().getBlockEntity();
        final BlockEntity target = self.getLevel()
                .getBlockEntity(self.getBlockPos().relative(this.getSide().getDirection()));
        final int newHandlerHash = this.createHandlerHash(target);

        if (newHandlerHash != 0 && newHandlerHash == this.handlerHash) {
            return this.handler;
        }

        this.handlerHash = newHandlerHash;
        this.handler = null;
        this.monitor = null;
        if (target != null) {
            IMEInventory<IAEItemStack> inv = this.getInventoryWrapper(target);

            if (inv instanceof MEMonitorIInventory h) {
                h.setMode((StorageFilter) this.getConfigManager().getSetting(Settings.STORAGE_FILTER));
            }

            if (inv instanceof ITickingMonitor) {
                this.monitor = (ITickingMonitor) inv;
                this.monitor.setActionSource(new MachineSource(this));
            }

            if (inv != null) {
                this.checkInterfaceVsStorageBus(target, this.getSide().getOpposite());

                this.handler = new MEInventoryHandler<IAEItemStack>(inv,
                        StorageChannels.items());

                this.handler.setBaseAccess((AccessRestriction) this.getConfigManager().getSetting(Settings.ACCESS));
                this.handler.setWhitelist(this.getInstalledUpgrades(Upgrades.INVERTER) > 0 ? IncludeExclude.BLACKLIST
                        : IncludeExclude.WHITELIST);
                this.handler.setPriority(this.priority);

                final IItemList<IAEItemStack> priorityList = StorageChannels.items().createList();

                final int slotsToUse = 18 + this.getInstalledUpgrades(Upgrades.CAPACITY) * 9;
                for (int x = 0; x < this.Config.getSlots() && x < slotsToUse; x++) {
                    final IAEItemStack is = this.Config.getAEStackInSlot(x);
                    if (is != null) {
                        priorityList.add(is);
                    }
                }

                if (this.getInstalledUpgrades(Upgrades.FUZZY) > 0) {
                    this.handler.setPartitionList(new FuzzyPriorityList<IAEItemStack>(priorityList,
                            (FuzzyMode) this.getConfigManager().getSetting(Settings.FUZZY_MODE)));
                } else {
                    this.handler.setPartitionList(new PrecisePriorityList<IAEItemStack>(priorityList));
                }

                if (inv instanceof IBaseMonitor) {
                    ((IBaseMonitor<IAEItemStack>) inv).addListener(this, this.handler);
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

    private void checkInterfaceVsStorageBus(final BlockEntity target, final AEPartLocation side) {
        IGridNode targetNode = null;

        if (target instanceof ItemInterfaceBlockEntity interfaceBlockEntity) {
            targetNode = interfaceBlockEntity.getMainNode().getNode();
        } else if (target instanceof IPartHost) {
            final Object part = ((IPartHost) target).getPart(side);
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
    public List<IMEInventoryHandler> getCellArray(final IStorageChannel channel) {
        if (channel == StorageChannels.items()) {
            final IMEInventoryHandler<IAEItemStack> out = this.getMainNode().isActive() ? this.getInternalHandler()
                    : null;
            if (out != null) {
                return Collections.singletonList(out);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public void setPriority(final int newValue) {
        this.priority = newValue;
        this.getHost().markForSave();
        this.resetCache(true);
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return AEParts.ITEM_STORAGE_BUS.stack();
    }

    @Override
    public MenuType<?> getMenuType() {
        return ItemStorageBusMenu.TYPE;
    }
}
