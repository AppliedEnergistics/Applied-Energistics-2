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

package appeng.parts.automation;

import appeng.api.behaviors.PlacementStrategy;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEKey;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigManagerBuilder;
import appeng.core.definitions.AEItems;
import appeng.helpers.IConfigInvHost;
import appeng.helpers.IPriorityHost;
import appeng.items.parts.PartModels;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.FormationPlaneMenu;
import appeng.menu.locator.MenuLocators;
import appeng.util.ConfigInventory;
import appeng.util.prioritylist.IPartitionList;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FormationPlanePart extends UpgradeablePart implements IStorageProvider, IPriorityHost, IConfigInvHost {

    private boolean wasOnline = false;
    private int priority = 0;
    private final PlaneConnectionHelper connectionHelper = new PlaneConnectionHelper(this);
    private final MEStorage inventory = new InWorldStorage();
    private final ConfigInventory config;
    @Nullable
    private PlacementStrategy placementStrategies;
    private IncludeExclude filterMode = IncludeExclude.WHITELIST;
    private IPartitionList filter;

    public FormationPlanePart(IPartItem<?> partItem) {
        super(partItem);
        getMainNode().addService(IStorageProvider.class, this);
        this.config = ConfigInventory.configTypes(63)
                .supportedTypes(StackWorldBehaviors.withPlacementStrategy())
                .changeListener(this::updateFilter)
                .build();
    }

    @Override
    protected void registerSettings(IConfigManagerBuilder builder) {
        super.registerSettings(builder);
        builder.registerSetting(Settings.PLACE_BLOCK, YesNo.YES);
        builder.registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
    }

    protected final PlacementStrategy getPlacementStrategies() {
        if (placementStrategies == null) {
            // Defer initialization until the grid exists
            var node = getMainNode().getNode();
            if (node == null) {
                return PlacementStrategy.noop();
            }
            var self = this.getHost().getBlockEntity();
            var pos = self.getBlockPos().relative(this.getSide());
            var side = getSide().getOpposite();
            var owningPlayerId = getMainNode().getNode().getOwningPlayerProfileId();
            placementStrategies = StackWorldBehaviors.createPlacementStrategies(
                    (ServerLevel) self.getLevel(), pos, side, self, owningPlayerId);
        }
        return placementStrategies;
    }

    protected final void updateFilter() {
        this.filter = createFilter();
        this.filterMode = isUpgradedWith(AEItems.INVERTER_CARD)
                ? IncludeExclude.BLACKLIST
                : IncludeExclude.WHITELIST;
    }

    @Override
    protected int getUpgradeSlots() {
        return 5;
    }

    @Override
    public void upgradesChanged() {
        this.updateFilter();
    }

    @Override
    public void onSettingChanged(IConfigManager manager, Setting<?> setting) {
        this.getHost().markForSave();
    }

    private void remountStorage() {
        IStorageProvider.requestUpdate(getMainNode());
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        var currentOnline = this.getMainNode().isOnline();
        if (this.wasOnline != currentOnline) {
            this.wasOnline = currentOnline;
            this.remountStorage();
            this.getHost().markForUpdate();
        }
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        connectionHelper.getBoxes(bch);
    }

    public PlaneConnections getConnections() {
        return connectionHelper.getConnections();
    }

    @Override
    public void onUpdateShape(Direction side) {
        var ourSide = getSide();
        // A block might have been changed in front of us
        if (side.equals(ourSide)) {
            if (!isClientSide()) {
                getPlacementStrategies().clearBlocked();
            }
        } else if (ourSide.getAxis() != side.getAxis()) {
            // Changes perpendicular to our side may change the connected plane model to change
            connectionHelper.updateConnections();
        }
    }

    /**
     * Places the given stacks in-world and returns what couldn't be placed.
     *
     * @return The amount that was placed.
     * @see MEStorage#insert
     */
    protected long placeInWorld(AEKey what, long amount, Actionable type) {
        var placeBlock = this.getConfigManager().getSetting(Settings.PLACE_BLOCK);

        return getPlacementStrategies().placeInWorld(what, amount, type, placeBlock != YesNo.YES);
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 1;
    }

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        this.priority = data.getIntOr("priority", 0);
        this.config.readFromChildTag(data, "config", registries);
        remountStorage();
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        data.putInt("priority", this.getPriority());
        this.config.writeToChildTag(data, "config", registries);
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public void setPriority(int newValue) {
        this.priority = newValue;
        this.getHost().markForSave();
        this.remountStorage();
    }

    @Override
    public void mountInventories(IStorageMounts mounts) {
        if (getMainNode().isOnline()) {
            // Update the filter at least once before registering the inventory
            updateFilter();
            mounts.mount(inventory, priority);
        }
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(getMenuType(), player, MenuLocators.forPart(this));
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(getPartItem());
    }

    private void openConfigMenu(Player player) {
        MenuOpener.open(getMenuType(), player, MenuLocators.forPart(this));
    }

    protected MenuType<?> getMenuType() {
        return FormationPlaneMenu.TYPE;
    }

    /**
     * Creates a partition list to filter stacks being injected into the plane against. If an inverter card is present,
     * it's a blacklist. If a fuzzy card is present and the storage channel supports fuzzy search, it'll be a list with
     * fuzzy support.
     */
    private IPartitionList createFilter() {
        var builder = IPartitionList.builder();
        if (isUpgradedWith(AEItems.FUZZY_CARD)) {
            builder.fuzzyMode(getConfigManager().getSetting(Settings.FUZZY_MODE));
        }
        var slotsToUse = 18 + this.getInstalledUpgrades(AEItems.CAPACITY_CARD) * 9;
        for (var x = 0; x < this.config.size() && x < slotsToUse; x++) {
            builder.add(this.config.getKey(x));
        }
        return builder.build();
    }

    /**
     * Models the block adjacent to this formation plane as storage.
     */
    class InWorldStorage implements MEStorage {
        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (filter != null && !filter.matchesFilter(what, filterMode)) {
                return 0;
            }

            return placeInWorld(what, amount, mode);
        }

        @Override
        public Component getDescription() {
            return getPartItem().asItem().getName();
        }
    }

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos) {
        if (!isClientSide()) {
            openConfigMenu(player);
        }
        return true;
    }

    @Override
    public ConfigInventory getConfig() {
        return config;
    }

    @Override
    public void collectModelData(ModelData.Builder builder) {
        super.collectModelData(builder);
        builder.with(PartModelData.CONNECTIONS, getConnections());
    }

}
