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

package appeng.parts.storagebus;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

import appeng.api.config.AccessRestriction;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.YesNo;
import appeng.api.features.IPlayerRegistry;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.core.stats.AdvancementTriggers;
import appeng.helpers.IConfigInvHost;
import appeng.helpers.IPriorityHost;
import appeng.helpers.InterfaceLogicHost;
import appeng.items.parts.PartModels;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.CompositeStorage;
import appeng.me.storage.ITickingMonitor;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.NullInventory;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.StorageBusMenu;
import appeng.menu.locator.MenuLocators;
import appeng.parts.PartAdjacentApi;
import appeng.parts.PartModel;
import appeng.parts.automation.ExternalStorageStrategy;
import appeng.parts.automation.StackWorldBehaviors;
import appeng.parts.automation.UpgradeablePart;
import appeng.util.ConfigInventory;
import appeng.util.SettingsFrom;
import appeng.util.prioritylist.IPartitionList;

public class StorageBusPart extends UpgradeablePart
        implements IGridTickable, IStorageProvider, IPriorityHost, IConfigInvHost {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "part/storage_bus_base");

    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/storage_bus_off"));

    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/storage_bus_on"));

    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/storage_bus_has_channel"));

    protected final IActionSource source;
    private final ConfigInventory config = ConfigInventory.configTypes(63, this::onConfigurationChanged);
    /**
     * This is the virtual inventory this storage bus exposes to the network it belongs to. To avoid continuous
     * cell-change notifications, we instead use a handler that will exist as long as this storage bus exists, while
     * changing the underlying inventory.
     */
    private final StorageBusInventory handler = new StorageBusInventory(NullInventory.of());
    @Nullable
    private Component handlerDescription;
    private final PartAdjacentApi<IStorageMonitorableAccessor> adjacentStorageAccessor;
    @Nullable
    private Map<AEKeyType, ExternalStorageStrategy> externalStorageStrategies;
    private boolean wasActive = false;
    private int priority = 0;
    /**
     * Indicates that the storage-bus should reevaluate the block it's attached to on the next tick and update the
     * target inventory - if necessary.
     */
    private boolean shouldUpdateTarget = true;
    private ITickingMonitor monitor = null;

    public StorageBusPart(IPartItem<?> partItem) {
        super(partItem);
        this.adjacentStorageAccessor = new PartAdjacentApi<>(this, IStorageMonitorableAccessor.SIDED);
        this.getConfigManager().registerSetting(Settings.ACCESS, AccessRestriction.READ_WRITE);
        this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.getConfigManager().registerSetting(Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY);
        this.getConfigManager().registerSetting(Settings.FILTER_ON_EXTRACT, YesNo.YES);
        this.source = new MachineSource(this);
        getMainNode()
                .addService(IStorageProvider.class, this)
                .addService(IGridTickable.class, this);
    }

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
        if (isClientSide()) {
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
        config.readFromChildTag(data, "config");
    }

    @Override
    public void writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putInt("priority", this.priority);
        config.writeToChildTag(data, "config");
    }

    @Override
    public final boolean onPartActivate(Player player, InteractionHand hand, Vec3 pos) {
        if (!isClientSide()) {
            openConfigMenu(player);
        }
        return true;
    }

    protected final void openConfigMenu(Player player) {
        MenuOpener.open(getMenuType(), player, MenuLocators.forPart(this));
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        openConfigMenu(player);
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(getPartItem());
    }

    public MenuType<?> getMenuType() {
        return StorageBusMenu.TYPE;
    }

    @Override
    public final void getBoxes(IPartCollisionHelper bch) {
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
    public final TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.StorageBus, false, true);
    }

    @Override
    public final TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (this.shouldUpdateTarget) {
            this.updateTarget(false);
            this.shouldUpdateTarget = false;
        }

        if (this.monitor != null) {
            return this.monitor.onTick();
        }

        return TickRateModulation.SLEEP;
    }

    /**
     * Used by the menu to configure based on stored contents.
     */
    public MEStorage getInternalHandler() {
        return this.handler.getDelegate();
    }

    private boolean hasRegisteredCellToNetwork() {
        return getMainNode().isActive() && !(this.handler.getDelegate() instanceof NullInventory);
    }

    public Component getConnectedToDescription() {
        return handlerDescription;
    }

    protected void onConfigurationChanged() {
        if (getMainNode().isReady()) {
            updateTarget(true);
        }
    }

    private void updateTarget(boolean forceFullUpdate) {
        if (isClientSide()) {
            return; // Part is not part of level yet or its client-side
        }

        MEStorage foundMonitor = null;
        Map<AEKeyType, MEStorage> foundExternalApi = Collections.emptyMap();

        // Prioritize a handler to directly link to another ME network
        var accessor = adjacentStorageAccessor.find();

        if (accessor != null) {
            var inventory = accessor.getInventory(this.source);
            if (inventory != null) {
                foundMonitor = inventory;
            }

            // So this could / can be a design decision. If the block entity does support our custom capability,
            // but it does not return an inventory for the action source, we do NOT fall back to using the external
            // API, as that might circumvent the security settings, and might also cause performance issues.
        } else {
            // Query all available external APIs
            // TODO: If a filter is configured, we might want to only query external APIs for compatible key spaces
            foundExternalApi = new IdentityHashMap<>(2);
            findExternalStorages(foundExternalApi);
        }

        if (!forceFullUpdate && this.handler.getDelegate() instanceof CompositeStorage compositeStorage
                && !foundExternalApi.isEmpty()) {
            // Just update the inventory reference, the ticking monitor will take care of the rest.
            compositeStorage.setStorages(foundExternalApi);
            handlerDescription = compositeStorage.getDescription();
            return;
        } else if (!forceFullUpdate && foundMonitor == this.handler.getDelegate()) {
            // Monitor didn't change, nothing to do!
            return;
        }

        var wasSleeping = this.monitor == null;
        var wasRegistered = this.hasRegisteredCellToNetwork();

        // Update inventory
        MEStorage newInventory;
        if (foundMonitor != null) {
            newInventory = foundMonitor;
            this.checkStorageBusOnInterface();
            handlerDescription = newInventory.getDescription();
        } else if (!foundExternalApi.isEmpty()) {
            newInventory = new CompositeStorage(foundExternalApi);
            handlerDescription = newInventory.getDescription();
        } else {
            newInventory = NullInventory.of();
            handlerDescription = null;
        }
        this.handler.setDelegate(newInventory);

        // Apply other settings.
        this.handler.setAccessRestriction(this.getConfigManager().getSetting(Settings.ACCESS));
        this.handler.setWhitelist(isUpgradedWith(AEItems.INVERTER_CARD) ? IncludeExclude.BLACKLIST
                : IncludeExclude.WHITELIST);

        this.handler.setPartitionList(createFilter());

        // Ensure we apply the partition list to the available items.
        boolean filterOnExtract = this.getConfigManager().getSetting(Settings.FILTER_ON_EXTRACT) == YesNo.YES;
        this.handler.setExtractFiltering(filterOnExtract, isExtractableOnly() && filterOnExtract);

        // Let the new inventory react to us ticking.
        if (newInventory instanceof ITickingMonitor tickingMonitor) {
            this.monitor = tickingMonitor;
        } else {
            this.monitor = null;
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

    private boolean isExtractableOnly() {
        return this.getConfigManager().getSetting(Settings.STORAGE_FILTER) == StorageFilter.EXTRACTABLE_ONLY;
    }

    private IPartitionList createFilter() {
        var filterBuilder = IPartitionList.builder();
        if (isUpgradedWith(AEItems.FUZZY_CARD)) {
            filterBuilder.fuzzyMode(this.getConfigManager().getSetting(Settings.FUZZY_MODE));
        }

        var slotsToUse = 18 + getInstalledUpgrades(AEItems.CAPACITY_CARD) * 9;
        for (var x = 0; x < config.size() && x < slotsToUse; x++) {
            filterBuilder.add(config.getKey(x));
        }
        return filterBuilder.build();
    }

    private void findExternalStorages(Map<AEKeyType, MEStorage> storages) {
        var extractableOnly = isExtractableOnly();
        for (var entry : getExternalStorageStrategies().entrySet()) {
            var wrapper = entry.getValue().createWrapper(
                    extractableOnly,
                    this::invalidateOnExternalStorageChange);
            if (wrapper != null) {
                storages.put(entry.getKey(), wrapper);
            }
        }
    }

    private void invalidateOnExternalStorageChange() {
        getMainNode().ifPresent((grid, node) -> {
            grid.getTickManager().alertDevice(node);
        });
    }

    private void checkStorageBusOnInterface() {
        var oppositeSide = getSide().getOpposite();
        var targetPos = getBlockEntity().getBlockPos().relative(getSide());
        var targetBe = getLevel().getBlockEntity(targetPos);

        Object targetHost = targetBe;
        if (targetBe instanceof IPartHost partHost) {
            targetHost = partHost.getPart(oppositeSide);
        }

        if (targetHost instanceof InterfaceLogicHost) {
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
    public final void setPriority(int newValue) {
        this.priority = newValue;
        this.getHost().markForSave();
        this.remountStorage();
    }

    /**
     * This inventory forwards to the actual external inventory and allows the inventory to be swapped out underneath.
     */
    private static class StorageBusInventory extends MEInventoryHandler {
        public StorageBusInventory(MEStorage inventory) {
            super(inventory);
        }

        @Override
        protected MEStorage getDelegate() {
            return super.getDelegate();
        }

        @Override
        protected void setDelegate(MEStorage delegate) {
            super.setDelegate(delegate);
        }

        public void setAccessRestriction(AccessRestriction setting) {
            setAllowExtraction(setting.isAllowExtraction());
            setAllowInsertion(setting.isAllowInsertion());
        }
    }

    @Override
    public ConfigInventory getConfig() {
        return this.config;
    }

    @Override
    public void importSettings(SettingsFrom mode, CompoundTag input, @Nullable Player player) {
        super.importSettings(mode, input, player);
        config.readFromChildTag(input, "config");
    }

    @Override
    public void exportSettings(SettingsFrom mode, CompoundTag output) {
        super.exportSettings(mode, output);

        if (mode == SettingsFrom.MEMORY_CARD) {
            config.writeToChildTag(output, "config");
        }
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

    private Map<AEKeyType, ExternalStorageStrategy> getExternalStorageStrategies() {
        if (externalStorageStrategies == null) {
            var host = getHost().getBlockEntity();
            this.externalStorageStrategies = StackWorldBehaviors.createExternalStorageStrategies(
                    (ServerLevel) host.getLevel(),
                    host.getBlockPos().relative(getSide()),
                    getSide().getOpposite());
        }
        return externalStorageStrategies;
    }
}
