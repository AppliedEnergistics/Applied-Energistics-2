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

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.IModelData;

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
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
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

public class FormationPlanePart extends UpgradeablePart implements IStorageProvider, IPriorityHost, IConfigInvHost {

    private static final PlaneModels MODELS = new PlaneModels("part/formation_plane",
            "part/formation_plane_on");

    private boolean wasActive = false;
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
        this.config = ConfigInventory.configTypes(StackWorldBehaviors.hasPlacementStrategy(),
                63, this::updateFilter);

        this.getConfigManager().registerSetting(Settings.PLACE_BLOCK, YesNo.YES);
        this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
    }

    protected final PlacementStrategy getPlacementStrategies() {
        if (placementStrategies == null) {
            var self = this.getHost().getBlockEntity();
            var pos = self.getBlockPos().relative(this.getSide());
            var side = getSide().getOpposite();
            placementStrategies = StackWorldBehaviors.createPlacementStrategies(
                    (ServerLevel) self.getLevel(), pos, side, self);
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

    public void stateChanged() {
        var currentActive = this.getMainNode().isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            this.remountStorage();
            this.getHost().markForUpdate();
        }
    }

    private void remountStorage() {
        IStorageProvider.requestUpdate(getMainNode());
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        stateChanged();
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        connectionHelper.getBoxes(bch);
    }

    public PlaneConnections getConnections() {
        return connectionHelper.getConnections();
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        if (pos.relative(this.getSide()).equals(neighbor)) {
            // The neighbor this plane is facing has changed
            if (!isClientSide()) {
                getPlacementStrategies().clearBlocked();
            }
        } else {
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

    /**
     * Indicates whether this formation plane supports placement of injected stacks as entities into the world.
     */
    public boolean supportsEntityPlacement() {
        for (int i = 0; i < config.size(); i++) {
            if (AEItemKey.is(config.getKey(i))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 1;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        this.priority = data.getInt("priority");
        this.config.readFromChildTag(data, "config");
        remountStorage();
    }

    @Override
    public void writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putInt("priority", this.getPriority());
        this.config.writeToChildTag(data, "config");
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
        if (getMainNode().isActive()) {
            // Update the filter at least once before registering the inventory
            updateFilter();
            mounts.mount(inventory, priority);
        }
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        openConfigMenu(player);
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
            return getPartItem().asItem().getDescription();
        }
    }

    @Override
    public boolean onPartActivate(Player player, InteractionHand hand, Vec3 pos) {
        if (!isClientSide()) {
            openConfigMenu(player);
        }
        return true;
    }

    @Override
    public ConfigInventory getConfig() {
        return config;
    }

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    @Override
    public IModelData getModelData() {
        return new PlaneModelData(getConnections());
    }

}
