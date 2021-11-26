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

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

import appeng.api.config.Actionable;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.storage.AEKeyFilter;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.MEStorage;
import appeng.api.storage.data.AEKey;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.helpers.IConfigInvHost;
import appeng.helpers.IPriorityHost;
import appeng.menu.ISubMenu;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.util.ConfigInventory;
import appeng.util.prioritylist.IPartitionList;

public abstract class FormationPlanePart extends UpgradeablePart
        implements IStorageProvider, IPriorityHost, IConfigInvHost {

    private boolean wasActive = false;
    private int priority = 0;
    private final PlaneConnectionHelper connectionHelper = new PlaneConnectionHelper(this);
    private final MEStorage inventory = new InWorldStorage();
    private final ConfigInventory config;
    private IncludeExclude filterMode = IncludeExclude.WHITELIST;
    private IPartitionList filter;
    /**
     * {@link System#currentTimeMillis()} of when the last sound/visual effect was played by this plane.
     */
    private long lastEffect;

    public FormationPlanePart(ItemStack is, AEKeyFilter filter) {
        super(is);
        getMainNode().addService(IStorageProvider.class, this);
        this.config = ConfigInventory.configTypes(filter, 63, this::updateFilter);
    }

    protected final void updateFilter() {
        this.filter = createFilter();
        this.filterMode = this.getInstalledUpgrades(Upgrades.INVERTER) > 0
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
            clearBlocked(level, neighbor);
        } else {
            connectionHelper.updateConnections();
        }
    }

    protected abstract void clearBlocked(BlockGetter level, BlockPos pos);

    /**
     * Places the given stacks in-world and returns what couldn't be placed.
     *
     * @return The amount that was placed.
     * @see MEStorage#insert
     */
    protected abstract long placeInWorld(AEKey input, long amount, Actionable type);

    /**
     * Indicates whether this formation plane supports placement of injected stacks as entities into the world.
     */
    public abstract boolean supportsEntityPlacement();

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 1;
    }

    @Override
    public void readFromNBT(final CompoundTag data) {
        super.readFromNBT(data);
        this.priority = data.getInt("priority");
        this.config.readFromChildTag(data, "config");
        remountStorage();
    }

    @Override
    public void writeToNBT(final CompoundTag data) {
        super.writeToNBT(data);
        data.putInt("priority", this.getPriority());
        this.config.writeToChildTag(data, "config");
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public void setPriority(final int newValue) {
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
        return getItemStack();
    }

    private void openConfigMenu(Player player) {
        MenuOpener.open(getMenuType(), player, MenuLocator.forPart(this));
    }

    protected abstract MenuType<?> getMenuType();

    /**
     * Creates a partition list to filter stacks being injected into the plane against. If an inverter card is present,
     * it's a blacklist. If a fuzzy card is present and the storage channel supports fuzzy search, it'll be a list with
     * fuzzy support.
     */
    private IPartitionList createFilter() {
        var builder = IPartitionList.builder();
        if (getInstalledUpgrades(Upgrades.FUZZY) > 0) {
            builder.fuzzyMode(getConfigManager().getSetting(Settings.FUZZY_MODE));
        }
        var slotsToUse = 18 + this.getInstalledUpgrades(Upgrades.CAPACITY) * 9;
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
    }

    /**
     * Only play the effect every 250ms.
     */
    protected final boolean throttleEffect() {
        long now = System.currentTimeMillis();
        if (now < lastEffect + 250) {
            return true;
        }
        lastEffect = now;
        return false;
    }

    @Override
    public boolean onPartActivate(final Player player, final InteractionHand hand, final Vec3 pos) {
        if (!isClientSide()) {
            openConfigMenu(player);
        }
        return true;
    }

    @Override
    public ConfigInventory getConfig() {
        return config;
    }

}
